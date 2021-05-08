FROM openjdk:11.0.5-jdk

MAINTAINER tinyshrimp@163.com

COPY . /tmp/yanagishima

ENV VERSION 22.0
ENV YANAGISHIMA_HOME /opt/yanagishima
ENV TMP_PATH /tmp/yanagishima

# install node
RUN curl -fsSL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get install -y nodejs build-essential

# deply yanagishima
RUN cd $TMP_PATH && git checkout -b 22.0 refs/tags/22.0 && \
    cd $TMP_PATH/web && npm install node-sass popper.js

RUN cd $TMP_PATH && ./gradlew distZip && \
    cd build/distributions && \
    unzip -d /opt yanagishima-$VERSION.zip && \
    ln -sf /opt/yanagishima-$VERSION $YANAGISHIMA_HOME && \
    sed -i 's/"$@" &/"$@"/g' $YANAGISHIMA_HOME/bin/yanagishima-start.sh && \
    rm -rf $TMP_PATH

WORKDIR $YANAGISHIMA_HOME

CMD bin/yanagishima-start.sh
