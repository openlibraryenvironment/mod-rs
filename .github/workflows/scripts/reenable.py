#!/usr/bin/python3

import argparse
import json
import re
import sys
import time
import urllib.request

# module names without version
MODULES = ['mod-rs-2.16.0-SLNP.001']

# names of tenants to operate on
TENANTS = ['slnptest_one', 'slnptest_two']

REGISTRY = "https://registry.reshare-dev.indexdata.com"

# for now hard code 8080
PORT = "8080"

def main():
    args = parse_command_line_args()
    action = args.action
    okapi_url = args.okapi_url
    username = args.username
    password = args.password
    registry = args.registry
    token = get_token(username, password, okapi_url)

    if action == "disable":
        disable_result = disable(args, token)
    elif action == "enable":
        enable_result = enable(args, token)
    elif action == "all":
        disable_result = disable(args, token)
        enable_result = enable(args, token)
    else:
        print("Unkown action: {}. User enable, disable, or all".format(action))

def disable(args, token):
    action = args.action
    okapi_url = args.okapi_url
    username = args.username
    password = args.password
    registry = args.registry

    disable_versions = []
 
    for module in MODULES:
        r = okapi_get(okapi_url +
                      '/_/proxy/tenants/{}/modules?filter={}'.format(TENANTS[0], module),
                      token=token)
        if (len(json.loads(r)) > 0):
            disable_versions.append({
                "id" : json.loads(r)[0]['id'],
                "action" : "disable"
            })

    # disable for all
    print("Disable current module")
    for tenant in TENANTS:
        try:
             r = okapi_post(okapi_url + '/_/proxy/tenants/{}/install'.format(tenant),
                 payload=json.dumps(disable_versions).encode('UTF-8'),
                 tenant='supertenant',
                 token=token
             )
        except:
            print("could not disable for {}, continuing".format(tenant))


def enable(args, token):
    action = args.action
    okapi_url = args.okapi_url
    username = args.username
    password = args.password
    registry = args.registry
    port = PORT
    latest_versions = ['mod-rs-2.16.0-SLNP.001']

    # re-enable modules
    print("re-enabling pods on tenants...")
    enable_payload = []
    for module in latest_versions:
        enable_payload.append(
            {
                "id" : module,
                "action" : "enable"
            })
    for tenant in TENANTS:
        print("enabling on {}".format(tenant))
        r = okapi_post(okapi_url + '/_/proxy/tenants/{}/install?loadSample%3Dtrue&loadReference%3Dtrue'.format(tenant),
            payload=json.dumps(enable_payload).encode('UTF-8'),
            tenant='supertenant',
            token=token
        )
        print(r)
        time.sleep(5)




def parse_command_line_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-a', '--action', help='disable, enable, all', default="all", required=False)
    parser.add_argument('-u', '--username', help='okapi super user username', required=True)
    parser.add_argument('-p', '--password', help='okapi super user password', required=True)
    parser.add_argument('-o', '--okapi-url', help='okapi url',
                        default='http://localhost:9130', required=False)
    parser.add_argument('-r', '--registry', help='registry to pull mds from',
                        default='http://folio-registry.dev.folio.org', required=False)

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
