#!/usr/bin/env bash

cd `dirname $0`

readonly KC_DIR="`pwd`/keycloak/keycloak-4.0.0.Final"
readonly KC_PS=`ps x | grep "${KC_DIR}/standalone" | grep -v grep | awk '{print $1}'`

## If exists Keycloak process, then kill
if [ -n "${KC_PS}" ]; then
  kill "${KC_PS}"
fi
