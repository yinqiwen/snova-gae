@echo off
set APP_LOCATION=%~dp0\war
@start javaw "-Dappengine.app.location=%APP_LOCATION%" -jar  AppCfgWrapper\AppCfgWrapper.jar %*