import requests


def get_access_token(instance, refresh_token):
    data = {
        "refreshToken": refresh_token
    }
    response = requests.post(instance + "/iaas/api/login", json=data, verify=False)
    if response.status_code != 200:
        print("Login failed")
        raise Exception(response.content)

    return response.json()["token"]


def get_deployment(instance, token, deployment_id):
    response = requests.get(
        instance + "/deployment/api/deployments/" + deployment_id + "?expand=project,resources,lastRequest,blueprint",
        headers={
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }, verify=False)
    if response.status_code != 200:
        print("Get deployment failed")
        raise Exception(response.content)
    return response.json()


def send_message(context, inputs):
    deployment_id = inputs["deploymentId"]

    instance = inputs["instance"]
    refresh_token = inputs["token"]
    slack_token = inputs["slack_token"]

    deployment_data = get_deployment(instance, get_access_token(instance, refresh_token), deployment_id)
    owner = deployment_data["ownedBy"]

    if "channel" in inputs and inputs["channel"] is not None:
        channel = inputs["channel"]
    else:
        channel = "@" + owner.split("@")[0]

    payload = {
        "channel": channel,
        "text": "Deployment started by %s" % owner
    }

    response = requests.post(
        "https://slack.com/api/chat.postMessage",
        headers={
            'Authorization': 'Bearer ' + slack_token,
            'Content-Type': 'application/json'
        },
        json=payload
    )

    if response.status_code != 200:
        print("Failed to send message")
        raise Exception(response.content)
