/*
This file based on encode_decode_test.c from XDelta3 sources.

Copyright (C) 2007 Ralf Junker
Copyright (C) 2016-2017 Boris Timofeev

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

#define SIZEOF_SIZE_T 4
#define SIZEOF_UNSIGNED_LONG_LONG 8

#include "xdelta/xdelta3/xdelta3.h"
#include "xdelta/xdelta3/xdelta3.c"

int code(int encode, FILE *in, FILE *src, FILE *out, int ignoreChecksum);

const int ERR_UNABLE_OPEN_PATCH = -5001;
const int ERR_UNABLE_OPEN_ROM = -5002;
const int ERR_UNABLE_OPEN_OUTPUT = -5003;
const int ERR_UNABLE_OPEN_SOURCE = -5004;
const int ERR_UNABLE_OPEN_MODIFIED = -5005;
const int ERR_WRONG_CHECKSUM = -5010;

int Java_org_emunix_unipatcher_patcher_XDelta_xdelta3apply(JNIEnv *env,
                                                         jobject this,
                                                         jstring patchPath,
                                                         jstring romPath,
                                                         jstring outputPath,
                                                         jboolean ignoreChecksum) {
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

    if (!patchFile) {
        return ERR_UNABLE_OPEN_PATCH;
    }

    if (!romFile) {
        fclose(patchFile);
        return ERR_UNABLE_OPEN_ROM;
    }

    if (!outputFile) {
        fclose(patchFile);
        fclose(romFile);
        return ERR_UNABLE_OPEN_OUTPUT;
    }

    ret = code(0, patchFile, romFile, outputFile, (int)ignoreChecksum);

    fclose(patchFile);
    fclose(romFile);
    fclose(outputFile);
    return ret;
}

int Java_org_emunix_unipatcher_patcher_XDelta_xdelta3create(JNIEnv *env,
                                                           jobject this,
                                                           jstring patchPath,
                                                           jstring sourcePath,
                                                           jstring modifiedPath) {
    int ret = 0;
    const char *patchName = (*env)->GetStringUTFChars(env, patchPath, NULL);
    const char *sourceName = (*env)->GetStringUTFChars(env, sourcePath, NULL);
    const char *modifiedName = (*env)->GetStringUTFChars(env, modifiedPath, NULL);

    FILE *patchFile = fopen(patchName, "wb");
    FILE *sourceFile = fopen(sourceName, "rb");
    FILE *modifiedFile = fopen(modifiedName, "rb");

    (*env)->ReleaseStringUTFChars(env, patchPath, patchName);
    (*env)->ReleaseStringUTFChars(env, sourcePath, sourceName);
    (*env)->ReleaseStringUTFChars(env, modifiedPath, modifiedName);

    if (!patchFile) {
        return ERR_UNABLE_OPEN_PATCH;
    }

    if (!sourceFile) {
        fclose(patchFile);
        return ERR_UNABLE_OPEN_SOURCE;
    }

    if (!modifiedFile) {
        fclose(patchFile);
        fclose(sourceFile);
        return ERR_UNABLE_OPEN_MODIFIED;
    }

    ret = code(1, modifiedFile, sourceFile, patchFile, 0);

    fclose(patchFile);
    fclose(sourceFile);
    fclose(modifiedFile);
    return ret;
}


int code(int encode, FILE *in, FILE *src, FILE *out, int ignoreChecksum) {
    int BUFFER_SIZE = 0x1000;

    int r, ret;
    xd3_stream stream;
    xd3_config config;
    xd3_source source;
    void *Input_Buf;
    int Input_Buf_Read;

    memset(&stream, 0, sizeof(stream));
    memset(&source, 0, sizeof(source));

    xd3_init_config(&config, 0);
    config.winsize = BUFFER_SIZE;
    if (ignoreChecksum) {
        config.flags |= XD3_ADLER32_NOVER;
    }
    xd3_config_stream(&stream, &config);

    source.blksize = BUFFER_SIZE;
    source.curblk = malloc(source.blksize);

    /* Load 1st block of stream. */
    r = fseek(src, 0, SEEK_SET);
    if (r)
        return r;
    source.onblk = fread((void *) source.curblk, 1, source.blksize, src);
    source.curblkno = 0;
    xd3_set_source(&stream, &source);

    Input_Buf = malloc(BUFFER_SIZE);

    fseek(in, 0, SEEK_SET);
    do {
        Input_Buf_Read = fread(Input_Buf, 1, BUFFER_SIZE, in);
        if (Input_Buf_Read < BUFFER_SIZE) {
            xd3_set_flags(&stream, XD3_FLUSH | stream.flags);
        }
        xd3_avail_input(&stream, Input_Buf, Input_Buf_Read);

process:
        if (encode)
            ret = xd3_encode_input(&stream);
        else
            ret = xd3_decode_input(&stream);

        switch (ret) {
            case XD3_INPUT:
                continue;

            case XD3_OUTPUT:
                r = fwrite(stream.next_out, 1, stream.avail_out, out);
                if (r != (int) stream.avail_out)
                    return r;
                xd3_consume_output(&stream);
                goto process;

            case XD3_GETSRCBLK:
                r = fseek(src, source.blksize * source.getblkno, SEEK_SET);
                if (r)
                    return r;
                source.onblk = fread((void *) source.curblk, 1, source.blksize, src);
                source.curblkno = source.getblkno;
                goto process;

            case XD3_GOTHEADER:
            case XD3_WINSTART:
            case XD3_WINFINISH:
                goto process;

            default:
                if (stream.msg != NULL) {
                    if (strcmp(stream.msg, "target window checksum mismatch") == 0)
                        return ERR_WRONG_CHECKSUM;
                }
                return ret;
        }
    }
    while (Input_Buf_Read == BUFFER_SIZE);

    free(Input_Buf);

    free((void *) source.curblk);
    xd3_close_stream(&stream);
    xd3_free_stream(&stream);

    return 0;
}