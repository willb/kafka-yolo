# kafka-yolo
a basic demo of image recognition / object detection with Kakfa, Yolo, and Quarkus

## Basics

There are several components in this demo:

1. a _frontend service_ that accepts image uploads over HTTP and sends them to a Kafka topic (`raw-images` by default)
2. a _processing service_ that uses a pretrained YOLO model to identify objects in images from the raw-image topic and write annotated images and object predictions to another topic
3. an _echo service_ (Java with Smallrye, natively compiled with Quarkus) that takes predictions from one topic and republishes them to another, while exposing results on a web page.

## Prerequisites

You'll need OpenShift 4.2 and [Open Data Hub](opendatahub.io) or AMQ Streams.  You'll also need to have a recent `oc` command in your `PATH`.

## Installation

Here's what you'll do:

1.  Set `KAFKA_BOOTSTRAP` to your Kafka bootstrap server; if you're running Open Data Hub, this will be `odh-message-bus-kafka-bootstrap`.
2.  Build and install the frontend app.
3.  Expose a route to the frontend app.
4.  Build the echo service, ensuring that the builder pod has enough capacity to run a native compilation.
5.  Install the image processor service.
6. Wait for the echo service build to complete.
7. Expose a route to the echo service.


Here's how you'll do it:

    # step 1
    export KAFKA_BOOTSTRAP=odh-message-bus-kafka-bootstrap
    
    # step 2
    export FRONTEND_REPO=https://github.com/radanalyticsio/streaming-lab#demo-20200227
    oc new-app \
        centos/python-36-centos7~${FRONTEND_REPO} \
        --context-dir=image-frontend -e KAFKA_BROKERS=${KAFKA_BOOTSTRAP}:9092 \
        -e KAFKA_TOPIC=raw-images --name=image-frontend
    
    # step 3
    oc create route edge --service=image-frontend
    
    # step 4
    export QUARKUS_BUILDER=quay.io/quarkus/ubi-quarkus-native-s2i:19.3.1-java8
    export ECHO_REPO=https://github.com/willb/image-echo#demo-20200227
    oc new-app \
        ${QUARKUS_BUILDER}~${ECHO_REPO} \
        --name=image-echo -e KAFKA_BROKER=$KAFKA_BOOTSTRAP
    oc delete build $(oc get build | grep image-echo | tail -1 | cut -f1 -d' ' )
    oc patch bc/image-echo \
        -p '{"spec":{"resources":{"limits":{"cpu":"4", "memory":"4Gi"}}}}'
    oc start-build bc/image-echo
    
    # step 5
    oc new-app quay.io/willbenton/image-processor:demo-20200227 \
        --name image-processor -e KAFKA_BROKERS=${KAFKA_BOOTSTRAP}:9092 \
        -e KAFKA_TOPIC_IN=raw-images -e KAFKA_TOPIC_OUT=echo_in
        
    # step 6
    oc logs -f $(oc get build | grep image-echo | tail -1 | cut -f1 -d' ' )
    
    # step 7
    oc create route edge --service=image-echo


## Interaction

There are two web services to interact with.  The image frontend is here:

- `oc get route/image-frontend -o=jsonpath --template="{.spec.host}"`

and the echo service is here:

- `oc get route/image-echo -o=jsonpath --template="{.spec.host}"`

The echo service supports two routes:

- `/messages.html` and
- `/messages/stream`

Open both in browser windows.  Then go to the frontend and upload an image. A good place to start is the [Open Images Dataset](https://storage.googleapis.com/openimages/web/index.html).  The echo service should update with the results of the prediction.

## Code

Here are some interesting code repositories:

- the [image processor service](https://github.com/willb/image-processor) (in Python, with Darkflow and YOLO),
- the [echo service](https://github.com/willb/image-echo) (in Java),
- the [image frontend](https://github.com/radanalyticsio/streaming-lab/tree/madrid-lab/image-frontend), and
- a [model service s2i](https://chapeau.freevariable.com/2018/10/model-s2i.html) and a [pipeline service s2i](https://github.com/willb/nachlass/).

