FROM openjdk:11-jdk AS build

ARG YANAGISHIMA_VERSION
WORKDIR /root/

# install node
RUN bash -lc "curl -sL https://deb.nodesource.com/setup_11.x | bash - \
    && curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - \
    && deb https://dl.yarnpkg.com/debian/ stable main | tee /etc/apt/sources.list.d/yarn.list"
RUN bash -lc "apt-get update -qq \
    && apt-get install -qq --no-install-recommends nodejs yarn \
    && rm -rf /var/lib/apt/lists/*"

# web
COPY web /root/yanagishima/web
RUN bash -lc "cd /root/yanagishima/web \
    && npm install \
    && npm install -f node-sass \
    && npm run build"
# package
COPY . /root/yanagishima
RUN bash -lc "cd /root/yanagishima \
    && ./gradlew :distZip"

###########################################################################
FROM openjdk:11-jre-stretch

ARG YANAGISHIMA_VERSION
WORKDIR /root/

COPY --from=build /root/yanagishima/build/distributions/yanagishima-$YANAGISHIMA_VERSION.zip .

RUN unzip yanagishima-$YANAGISHIMA_VERSION.zip \
    && rm -f yanagishima-$YANAGISHIMA_VERSION.zip

EXPOSE 8080

CMD cd yanagishima-$YANAGISHIMA_VERSION \
    && ./bin/yanagishima-start.sh
