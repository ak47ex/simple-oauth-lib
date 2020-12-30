package com.suenara.simpleoauthlib.impl

enum class OauthParameter(val key: String) {
    CODE("code"),
    ACCESS_TOKEN("access_token"),
    TOKEN_TYPE("token_type"),
    EXPIRES_IN("expires_in"),
    REFRESH_TOKEN("refresh_token"),
    SCOPE("scope"),
    ID_TOKEN("id_token"),

    ERROR("error"),
    ERROR_DESCRIPTION("error_description"),
    ERROR_URI("error_uri"),
}