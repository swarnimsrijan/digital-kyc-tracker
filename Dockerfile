FROM 813361731051.dkr.ecr.ap-south-1.amazonaws.com/zeta-openjdk:2.0.0

# environment variable `APP` derived from arg `app` is used in entrypoint.sh
ARG app
ARG version
ARG lastCommitHash
ARG lastCommitAuthorEmail

ENV ARTIFACT_NAME $app
ENV ARTIFACT_VERSION $version
ENV ARTIFACT_COMMIT_ABR $lastCommitHash
ENV ARTIFACT_COMMITTER $lastCommitAuthorEmail
ENV APP $app
ENV JAR_PATH "$DATA_PATH/$APP-exec.jar"
ENV L4O2_CONFIG_LOCATION $DATA_PATH
ENV logging.config $DATA_PATH/log4Olympus2Config.xml

# [for log4Olympus] make applicaiton specific logging directory
RUN mkdir -p "$LOGS_PATH/$APP" && chown -R $USERNAME:$USERNAME "$LOGS_PATH/$APP"

# copy applicaiton jar into the image
COPY --chown=$USERNAME:$USERNAME ./target/${APP}-${version}.jar $JAR_PATH

# [IMP] We should never run as as root user
USER $USERNAME:$USERNAME