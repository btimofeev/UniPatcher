/*
This file based on encode_decode_test.c from XDelta3 sources.

Copyright (C) 2007 Ralf Junker
Copyright (C) 2016 Boris Timofeev

This file is part of UniPatcher.

UniPatcher is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

UniPatcher is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#define SIZEOF_SIZE_T 4
#define SIZEOF_UNSIGNED_LONG_LONG 8

#include "xdelta3/xdelta3/xdelta3.h"
#include "xdelta3/xdelta3/xdelta3.c"

int apply(FILE *patch, FILE *in, FILE *out);

const int ERR_UNABLE_OPEN_PATCH = -5001;
const int ERR_UNABLE_OPEN_ROM = -5002;
const int ERR_UNABLE_OPEN_OUTPUT = -5003;
const int ERR_WRONG_CHECKSUM = -5010;

int Java_org_emunix_unipatcher_patch_XDelta_xdelta3apply(JNIEnv *env,
            jobject this,
            jstring patchPath,
            jstring romPath,
            jstring outputPath)
{
    int ret = 0;
    const char *patchName = (*env)->GetStringUTFChars(env, patchPath, NULL);
    const char *romName = (*env)->GetStringUTFChars(env, romPath, NULL);
    const char *outputName = (*env)->GetStringUTFChars(env, outputPath, NULL);

    FILE *patchFile = fopen(patchName, "rb");
    FILE *romFile = fopen(romName, "rb");
    FILE *outputFile = fopen(outputName, "wb");

    (*env)->ReleaseStringUTFChars(env, patchPath, patchName);
    (*env)->ReleaseStringUTFChars(env, romPath, romName);
    (*env)->ReleaseStringUTFChars(env, outputPath, outputName);

    if (!patchFile)
    {
        return ERR_UNABLE_OPEN_PATCH;
    }

    if (!romFile)
    {
        fclose(patchFile);
        return ERR_UNABLE_OPEN_ROM;
    }

    if (!outputFile)
    {
        fclose(patchFile);
        fclose(romFile);
        return ERR_UNABLE_OPEN_OUTPUT;
    }

    ret = apply(patchFile, romFile, outputFile);

    fclose(patchFile);
    fclose(romFile);
    fclose(outputFile);
    return ret;
}

int apply(FILE *patch, FILE *in, FILE *out)
{
    int BUFFER_SIZE = 32768;

    int r, ret;
    xd3_stream stream;
    xd3_config config;
    xd3_source source;
    void* Input_Buf;
    int Input_Buf_Read;

    memset (&stream, 0, sizeof (stream));
    memset (&source, 0, sizeof (source));

    xd3_init_config(&config, 0);
    config.winsize = BUFFER_SIZE;
    xd3_config_stream(&stream, &config);

    source.blksize = BUFFER_SIZE;
    source.curblk = malloc(source.blksize);

    /* Load 1st block of stream. */
    r = fseek(in, 0, SEEK_SET);
    if (r)
        return r;
    source.onblk = fread((void*)source.curblk, 1, source.blksize, in);
    source.curblkno = 0;
    xd3_set_source(&stream, &source);

    Input_Buf = malloc(BUFFER_SIZE);

    fseek(patch, 0, SEEK_SET);
    do
    {
        Input_Buf_Read = fread(Input_Buf, 1, BUFFER_SIZE, patch);
        if (Input_Buf_Read < BUFFER_SIZE)
        {
            xd3_set_flags(&stream, XD3_FLUSH | stream.flags);
        }
        xd3_avail_input(&stream, Input_Buf, Input_Buf_Read);

        process:

        ret = xd3_decode_input(&stream);

        switch (ret)
        {
            case XD3_INPUT:
                continue;

            case XD3_OUTPUT:
                r = fwrite(stream.next_out, 1, stream.avail_out, out);
                if (r != (int)stream.avail_out)
                    return r;
                xd3_consume_output(&stream);
                goto process;

            case XD3_GETSRCBLK:
                r = fseek(in, source.blksize * source.getblkno, SEEK_SET);
                if (r)
                    return r;
                source.onblk = fread((void*)source.curblk, 1, source.blksize, in);
                source.curblkno = source.getblkno;
                goto process;

            case XD3_GOTHEADER:
            case XD3_WINSTART:
            case XD3_WINFINISH:
                goto process;

            default:
                __android_log_print(ANDROID_LOG_ERROR, "XDelta3", "Error %d: %s", ret, stream.msg);
                if (stream.msg != NULL) {
                    if (strcmp(stream.msg, "target window checksum mismatch") == 0)
                        return ERR_WRONG_CHECKSUM;
                }
                return ret;
        }
    }
    while (Input_Buf_Read == BUFFER_SIZE);

    free(Input_Buf);

    free((void*)source.curblk);
    xd3_close_stream(&stream);
    xd3_free_stream(&stream);

    return 0;
}