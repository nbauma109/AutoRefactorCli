#!/bin/bash -e
#
# usage:
#     autorefactor --help
#

DIR=`dirname $0`
BASE_DIR=`dirname "$DIR"`
BASE_DIR=`cd "$BASE_DIR"; pwd`

[ "$AUTOREFACTOR_ECLIPSE" = "" ] && AUTOREFACTOR_ECLIPSE=eclipse

WORKSPACE_DIR=`mktemp --tmpdir -d workspaceXXXXXX`
#trap "{ [ -n \"$WORKSPACE_DIR\" ] && rm -rf \"$WORKSPACE_DIR\"; }" EXIT

CONSOLE_LOG=""
[ $# -gt 0 -a "$1" = "--consolelog" ] && CONSOLE_LOG="-consolelog" && shift

# eclipse command line parameters:
# https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html

args=( "$@" )
args+=("-vmargs" "-Xmx2000m" "-Dorg.eclipse.equinox.p2.reconciler.dropins.directory=${BASE_DIR}/dropins")

#WORKSPACE_DIR=/tmp/workspace-autorefactor
$AUTOREFACTOR_ECLIPSE -nosplash $CONSOLE_LOG --launcher.suppressErrors -data "$WORKSPACE_DIR" -configuration "$WORKSPACE_DIR/configuration" -application org.autorefactor.cli.AutoRefactor "${args[@]}"
