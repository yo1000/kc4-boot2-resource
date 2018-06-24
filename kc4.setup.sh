#!/usr/bin/env bash

cd `dirname $0`
DIR=`pwd`

## Download and Extract Keycloak
if [ ! -d keycloak ]; then
  mkdir keycloak
fi
cd keycloak

if [ ! -d keycloak-4.0.0.Final ]; then
  curl -O https://downloads.jboss.org/keycloak/4.0.0.Final/keycloak-4.0.0.Final.tar.gz
  tar -zxvf keycloak-4.0.0.Final.tar.gz
fi
cd keycloak-4.0.0.Final

## If exists Keycloak process, then exit
if [ -n "`ps x | grep keycloak | grep -v grep`" ]; then
  echo 'Keycloak has already started.'
  exit 1
fi

## If exists using 8080 port process, then exit
if [ -n "`netstat -an | grep LISTEN | awk '{print $4}' | grep '\.8080'`" ]; then
  echo 'Port 8080 has already used by another process.'
  exit 1
fi

## Create Wildfly admin
bin/add-user.sh \
  -u wildfly \
  -p wildfly1234

## Create Keycloak admin
bin/add-user-keycloak.sh \
  -r master \
  -u keycloak \
  -p keycloak1234

bin/standalone.sh \
  -b 0.0.0.0 &

while [ -z "`netstat -an | grep '127.0.0.1.9990' | grep LISTEN`" ]; do sleep 10; done; echo "keycloak ok"

## Login to Keycloak
bin/kcadm.sh config credentials \
  --server http://127.0.0.1:8080/auth \
  --realm master \
  --user keycloak \
  --password keycloak1234

## Set vars
readonly REALM="kc4-boot2"
readonly ROLE_ADMIN="admin"
readonly ROLE_USER="user"
readonly USERNAME_ADMIN="alice"
readonly USERNAME_USER="bob"
readonly CLIENT_RESOURCE_SERVER="${REALM}-server"
readonly CLIENT_RESOURCE_CLIENT="${REALM}-client"
readonly ALLOW_REDIRECT_FROM1="http://127.0.0.1:8081/*"
readonly ALLOW_REDIRECT_FROM2="http://localhost:8081/*"

## Create Realm
bin/kcadm.sh create realms \
  -s realm=${REALM} \
  -s enabled=true

## Create Roles
bin/kcadm.sh create roles \
  -r ${REALM} \
  -s name=${ROLE_ADMIN}
bin/kcadm.sh create roles \
  -r ${REALM} \
  -s name=${ROLE_USER}

## Create Users
bin/kcadm.sh create users \
  -r ${REALM} \
  -s username=${USERNAME_ADMIN} \
  -s enabled=true
bin/kcadm.sh create users \
  -r ${REALM} \
  -s username=${USERNAME_USER} \
  -s enabled=true

## Update Password
bin/kcadm.sh set-password \
  -r ${REALM} \
  --username ${USERNAME_ADMIN} \
  -p "${USERNAME_ADMIN}1234"
bin/kcadm.sh set-password \
  -r ${REALM} \
  --username ${USERNAME_USER} \
  -p "${USERNAME_USER}1234"

## Assign Role to Users
bin/kcadm.sh add-roles \
  -r ${REALM} \
  --uusername ${USERNAME_ADMIN} \
  --rolename ${ROLE_ADMIN} \
  --rolename ${ROLE_USER}
bin/kcadm.sh add-roles \
  -r ${REALM} \
  --uusername ${USERNAME_USER} \
  --rolename ${ROLE_USER}

## Create Clients
RES_SRV_ID=`bin/kcadm.sh create clients -r ${REALM} -s clientId=${CLIENT_RESOURCE_SERVER} -s bearerOnly=true -i`; \
  echo "Created new client with id '${RES_SRV_ID}'"
RES_CLI_ID=`bin/kcadm.sh create clients -r ${REALM} -s clientId=${CLIENT_RESOURCE_CLIENT} -s "redirectUris=[\"${ALLOW_REDIRECT_FROM1}\", \"${ALLOW_REDIRECT_FROM2}\"]" -i`; \
  echo "Created new client with id '${RES_CLI_ID}'"

## Install keycloak.json
bin/kcadm.sh get clients/${RES_SRV_ID}/installation/providers/keycloak-oidc-keycloak-json \
  -r ${REALM} >"${DIR}/kc4-boot2-resource-server/src/main/resources/keycloak.json"
bin/kcadm.sh get clients/${RES_CLI_ID}/installation/providers/keycloak-oidc-keycloak-json \
  -r ${REALM} >"${DIR}/kc4-boot2-resource-client/src/main/resources/keycloak.json"
