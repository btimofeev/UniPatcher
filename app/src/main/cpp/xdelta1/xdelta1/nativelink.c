/* 
	Native JNI linkage
						*/
#include <jni.h>
#include <stdio.h>
#include <glib.h>
extern gint    main    (gint argc, gchar** argv);
/*int Java_com_xperia64_rompatcher_MainActivity_xdelta1PatchRom(JNIEnv * env, jobject this, jstring romPath, jstring patchPath, jstring outputFile)*/
int Java_org_emunix_unipatcher_patcher_XDelta_xdelta1apply(JNIEnv * env, jobject this, jstring patchPath, jstring romPath, jstring outputFile)
{
	jboolean isCopy;
	const gchar * szRomPath = (*env)->GetStringUTFChars(env, romPath, &isCopy);
	const gchar * szPatchPath = (*env)->GetStringUTFChars(env, patchPath, &isCopy);
	const gchar * szOutputFile = (*env)->GetStringUTFChars(env, outputFile, &isCopy);
	gchar** filez = NULL;
	filez = malloc(sizeof(gchar*) * 5);
	filez[0]="xdelta1"; // argv
	filez[1]="patch";
	filez[2]=(gchar*)szPatchPath;
	filez[3]=(gchar*)szRomPath;
	filez[4]=(gchar*)szOutputFile;
	int r = (int)main(5,filez);
	(*env)->ReleaseStringUTFChars(env, romPath, szRomPath); 
	(*env)->ReleaseStringUTFChars(env, patchPath, szPatchPath); 
	(*env)->ReleaseStringUTFChars(env, outputFile, szOutputFile); 
	free(filez);
	return r;
}

