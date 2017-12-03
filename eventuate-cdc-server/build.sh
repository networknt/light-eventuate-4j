#!/bin/bash

set -ex

IMAGE_NAME="networknt/eventuate-cdcserver"
TAG="${1}"

if docker inspect ${IMAGE_NAME} &> /dev/null; then
	docker rmi -f ${IMAGE_NAME}:latest
	docker rmi -f ${IMAGE_NAME}:${TAG}
fi

docker build -t ${IMAGE_NAME} .
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:latest
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:${TAG}
docker push ${IMAGE_NAME}
