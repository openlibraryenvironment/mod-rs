#!/usr/bin/python3

import argparse
import json
import re
import sys
import time
import urllib.request

# module names without version
MODULES = ['mod-rs']

# names of tenants to operate on
TENANTS = ['reshare_east', 'reshare_west']

REGISTRY = "https://registry.reshare-dev.indexdata.com"

def main():
    print("DISABLE SCRIPT")

def main_in_progress():
    args = parse_command_line_args()
    okapi_url = args.okapi_url
    username = args.username
    password = args.password
    token = get_token(username, password, okapi_url)

    # get currently enabled module versions
    disable_versions = []
    # for now just enable the exact same versions
    #enable_versions = []

    # get new versions from registry
    r = okapi_get(REGISTRY +
                  '/_/proxy/modules?filter=mod-rs&latest=1',
                  tenant='supertenant')
    new_rs_version = json.loads(r)[0]['id']
    r = okapi_get(REGISTRY +
                  '/_/proxy/modules?filter=mod-directory&latest=1',
                  tenant='supertenant')
    new_directory_version = json.loads(r)[0]['id']

    for module in RESHARE_MODULES:
        r = okapi_get(okapi_url +
                      '/_/proxy/tenants/{}/modules?filter={}'.format(TENANTS[0], module),
                      token=token)
        if (len(json.loads(r)) > 0):
            disable_versions.append({
                "id" : json.loads(r)[0]['id'],
                "action" : "disable"
            })

    enable_versions = [{
        "id" : new_rs_version,
        "action" : "enable"
    },{
        "id" : new_directory_version,
        "action" : "enable"
    }]

    # disable for both tenants
    print("Disable current reshare backend modules")
    for tenant in TENANTS:
        r = okapi_post(okapi_url + '/_/proxy/tenants/{}/install'.format(tenant),
            payload=json.dumps(disable_versions).encode('UTF-8'),
            tenant='supertenant',
            token=token
        )

def parse_command_line_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-u', '--username', help='okapi super user username', required=True)
    parser.add_argument('-p', '--password', help='okapi super user password', required=True)
    parser.add_argument('-o', '--okapi-url', help='okapi url',
                        default='https://okapi-reshare-1.folio-dev-us-east-1-1.folio-dev.indexdata.com', required=False)

    args = parser.parse_args()

    return args

def okapi_get(url, tenant=None, token=''):
    tenant = tenant or 'supertenant'
    headers = {
        'X-Okapi-Tenant': tenant,
        'X-Okapi-Token' : token,
        'Accept': 'application/json'
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        r = urllib.request.urlopen(req)
        response_data = r.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        sys.exit(
            ' - '.join([
                'ERROR', 'GET', e.url,
                str(e.status), str(e.read())
            ]))
    return response_data

def okapi_post(url, payload, tenant='supertenant', token='', return_headers=False):
    tenant = tenant or 'supertenant'
    headers = {
        'X-Okapi-Tenant': tenant,
        'X-Okapi-Token': token,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    }
    req = urllib.request.Request(url, data=payload, headers=headers)
    try:
        resp = urllib.request.urlopen(req)
        if return_headers == True:
            response_data = dict(resp.info())
        else:
            response_data =  resp.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        sys.exit(' - '.join([
                'ERROR', 'POST', e.url,
                str(e.status), str(e.read().decode('utf-8'))
            ]))
    return response_data

def okapi_delete(url, tenant='supertenant', token='', return_headers=False):
    tenant = tenant or 'supertenant'
    headers = {
        'X-Okapi-Tenant': tenant,
        'X-Okapi-Token': token,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    }
    req = urllib.request.Request(url, headers=headers, method='DELETE')
    with urllib.request.urlopen(req) as resp:
        resp_code = resp.getcode()
        return(resp_code)

def get_token(username, password, okapi_url):
    payload = json.dumps({
        'username': username,
        'password': password
    }).encode('UTF-8')
    r = okapi_post(okapi_url + '/authn/login', payload, return_headers=True)
    return r['x-okapi-token']

if __name__ == "__main__":
   main()
