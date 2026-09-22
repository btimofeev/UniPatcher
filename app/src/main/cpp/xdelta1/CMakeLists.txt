project(xdelta1)

set(XDELTA1_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set(XD_INCLUDES
    ${XDELTA1_DIR}
    ${XDELTA1_DIR}/glib
    ${XDELTA1_DIR}/glib/glib
    ${XDELTA1_DIR}/glib/android)

# ---- pcre (static, dependency of libglib-2.0) ----
add_library(pcre STATIC
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_compile.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_chartables.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_config.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_dfa_exec.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_exec.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_fullinfo.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_get.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_globals.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_newline.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_ord2utf8.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_study.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_tables.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_try_flipped.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_ucp_searchfuncs.c
    ${XDELTA1_DIR}/glib/glib/pcre/pcre_xclass.c)

target_compile_definitions(pcre PRIVATE
    G_LOG_DOMAIN=\"GLib-GRegex\"
    SUPPORT_UCP
    SUPPORT_UTF8
    NEWLINE=-1
    MATCH_LIMIT=10000000
    MATCH_LIMIT_RECURSION=10000000
    MAX_NAME_SIZE=32
    MAX_NAME_COUNT=10000
    MAX_DUPLENGTH=30000
    LINK_SIZE=2
    PCRE_STATIC
    POSIX_MALLOC_THRESHOLD=10)

target_include_directories(pcre PRIVATE
    ${XDELTA1_DIR}/glib/android-internal
    ${XDELTA1_DIR}/glib/glib/pcre
    ${XD_INCLUDES})

# ---- libglib-2.0 ----
add_library(glib-2.0 SHARED
    ${XDELTA1_DIR}/glib/glib/garray.c
    ${XDELTA1_DIR}/glib/glib/gasyncqueue.c
    ${XDELTA1_DIR}/glib/glib/gatomic.c
    ${XDELTA1_DIR}/glib/glib/gbacktrace.c
    ${XDELTA1_DIR}/glib/glib/gbase64.c
    ${XDELTA1_DIR}/glib/glib/gbitlock.c
    ${XDELTA1_DIR}/glib/glib/gbookmarkfile.c
    ${XDELTA1_DIR}/glib/glib/gbuffer.c
    ${XDELTA1_DIR}/glib/glib/gcache.c
    ${XDELTA1_DIR}/glib/glib/gchecksum.c
    ${XDELTA1_DIR}/glib/glib/gcompletion.c
    ${XDELTA1_DIR}/glib/glib/gconvert.c
    ${XDELTA1_DIR}/glib/glib/gdataset.c
    ${XDELTA1_DIR}/glib/glib/gdate.c
    ${XDELTA1_DIR}/glib/glib/gdatetime.c
    ${XDELTA1_DIR}/glib/glib/gdir.c
    ${XDELTA1_DIR}/glib/glib/gerror.c
    ${XDELTA1_DIR}/glib/glib/gfileutils.c
    ${XDELTA1_DIR}/glib/glib/ghash.c
    ${XDELTA1_DIR}/glib/glib/ghook.c
    ${XDELTA1_DIR}/glib/glib/ghostutils.c
    ${XDELTA1_DIR}/glib/glib/giochannel.c
    ${XDELTA1_DIR}/glib/glib/gkeyfile.c
    ${XDELTA1_DIR}/glib/glib/glist.c
    ${XDELTA1_DIR}/glib/glib/gmain.c
    ${XDELTA1_DIR}/glib/glib/gmappedfile.c
    ${XDELTA1_DIR}/glib/glib/gmarkup.c
    ${XDELTA1_DIR}/glib/glib/gmem.c
    ${XDELTA1_DIR}/glib/glib/gmessages.c
    ${XDELTA1_DIR}/glib/glib/gnode.c
    ${XDELTA1_DIR}/glib/glib/goption.c
    ${XDELTA1_DIR}/glib/glib/gpattern.c
    ${XDELTA1_DIR}/glib/glib/gpoll.c
    ${XDELTA1_DIR}/glib/glib/gprimes.c
    ${XDELTA1_DIR}/glib/glib/gqsort.c
    ${XDELTA1_DIR}/glib/glib/gqueue.c
    ${XDELTA1_DIR}/glib/glib/grel.c
    ${XDELTA1_DIR}/glib/glib/grand.c
    ${XDELTA1_DIR}/glib/glib/gregex.c
    ${XDELTA1_DIR}/glib/glib/gscanner.c
    ${XDELTA1_DIR}/glib/glib/gsequence.c
    ${XDELTA1_DIR}/glib/glib/gshell.c
    ${XDELTA1_DIR}/glib/glib/gslice.c
    ${XDELTA1_DIR}/glib/glib/gslist.c
    ${XDELTA1_DIR}/glib/glib/gstdio.c
    ${XDELTA1_DIR}/glib/glib/gstrfuncs.c
    ${XDELTA1_DIR}/glib/glib/gstring.c
    ${XDELTA1_DIR}/glib/glib/gtestutils.c
    ${XDELTA1_DIR}/glib/glib/gthread.c
    ${XDELTA1_DIR}/glib/glib/gthreadpool.c
    ${XDELTA1_DIR}/glib/glib/gtimer.c
    ${XDELTA1_DIR}/glib/glib/gtimezone.c
    ${XDELTA1_DIR}/glib/glib/gtree.c
    ${XDELTA1_DIR}/glib/glib/guniprop.c
    ${XDELTA1_DIR}/glib/glib/gutf8.c
    ${XDELTA1_DIR}/glib/glib/gunibreak.c
    ${XDELTA1_DIR}/glib/glib/gunicollate.c
    ${XDELTA1_DIR}/glib/glib/gunidecomp.c
    ${XDELTA1_DIR}/glib/glib/gurifuncs.c
    ${XDELTA1_DIR}/glib/glib/gutils.c
    ${XDELTA1_DIR}/glib/glib/gvariant.c
    ${XDELTA1_DIR}/glib/glib/gvariant-core.c
    ${XDELTA1_DIR}/glib/glib/gvariant-parser.c
    ${XDELTA1_DIR}/glib/glib/gvariant-serialiser.c
    ${XDELTA1_DIR}/glib/glib/gvarianttypeinfo.c
    ${XDELTA1_DIR}/glib/glib/gvarianttype.c
    ${XDELTA1_DIR}/glib/glib/gprintf.c
    ${XDELTA1_DIR}/glib/glib/giounix.c
    ${XDELTA1_DIR}/glib/glib/gspawn.c
    ${XDELTA1_DIR}/glib/glib/libcharset/localcharset.c)

target_compile_definitions(glib-2.0 PRIVATE
    LIBDIR=\"\"
    HAVE_CONFIG_H=1
    G_LOG_DOMAIN=\"GLib\"
    PCRE_STATIC
    G_DISABLE_DEPRECATED
    GLIB_COMPILATION)

target_include_directories(glib-2.0 PRIVATE
    ${XDELTA1_DIR}/glib/glib/libcharset
    ${XDELTA1_DIR}/glib/glib/gnulib
    ${XDELTA1_DIR}/glib/glib/pcre
    ${XDELTA1_DIR}/glib/android-internal
    ${XD_INCLUDES})

target_link_libraries(glib-2.0 PRIVATE pcre log)

# ---- libgmodule-2.0 ----
add_library(gmodule-2.0 SHARED ${XDELTA1_DIR}/glib/gmodule/gmodule.c)

target_compile_definitions(gmodule-2.0 PRIVATE
    HAVE_CONFIG_H=1
    G_LOG_DOMAIN=\"GModule\"
    G_DISABLE_DEPRECATED)

target_include_directories(gmodule-2.0 PRIVATE
    ${XDELTA1_DIR}/glib/gmodule/android-internal
    ${XDELTA1_DIR}/glib/android-internal
    ${XD_INCLUDES})

target_link_libraries(gmodule-2.0 PRIVATE glib-2.0 dl)

# ---- libedsio ----
add_library(edsio SHARED
    ${XDELTA1_DIR}/libedsio/base64.c
    ${XDELTA1_DIR}/libedsio/default.c
    ${XDELTA1_DIR}/libedsio/edsio.c
    ${XDELTA1_DIR}/libedsio/edsio_edsio.c
    ${XDELTA1_DIR}/libedsio/fh.c
    ${XDELTA1_DIR}/libedsio/generic.c
    ${XDELTA1_DIR}/libedsio/library.c
    ${XDELTA1_DIR}/libedsio/maketime.c
    ${XDELTA1_DIR}/libedsio/md5c.c
    ${XDELTA1_DIR}/libedsio/partime.c
    ${XDELTA1_DIR}/libedsio/sha.c
    ${XDELTA1_DIR}/libedsio/simple.c)

target_compile_options(edsio PRIVATE -O3)

target_include_directories(edsio PRIVATE
    ${XDELTA1_DIR}/libedsio
    ${XDELTA1_DIR}/glib
    ${XDELTA1_DIR}/glib/glib
    ${XDELTA1_DIR}/glib/gmodule
    ${XDELTA1_DIR}/glib/android)

target_link_libraries(edsio PRIVATE glib-2.0 gmodule-2.0 log)

# ---- libxdelta1 ----
add_library(xdelta1 SHARED
    ${XDELTA1_DIR}/xdelta1/getopt.c
    ${XDELTA1_DIR}/xdelta1/getopt1.c
    ${XDELTA1_DIR}/xdelta1/xd_edsio.c
    ${XDELTA1_DIR}/xdelta1/xdapply.c
    ${XDELTA1_DIR}/xdelta1/xdelta.c
    ${XDELTA1_DIR}/xdelta1/xdmain.c
    ${XDELTA1_DIR}/xdelta1/nativelink.c)

target_compile_options(xdelta1 PRIVATE -O3)

target_include_directories(xdelta1 PRIVATE
    ${XDELTA1_DIR}/xdelta1
    ${XDELTA1_DIR}/glib
    ${XDELTA1_DIR}/glib/glib
    ${XDELTA1_DIR}/glib/android
    ${XDELTA1_DIR}/libedsio)

target_link_libraries(xdelta1 PRIVATE glib-2.0 edsio z)