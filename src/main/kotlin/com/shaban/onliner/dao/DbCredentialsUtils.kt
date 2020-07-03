package com.shaban.onliner.dao

import com.shaban.onliner.dao.heroku.getHerokuJawsDbMariaCredentials

fun getDbCredentials(): DbCredentials {
    return getHerokuJawsDbMariaCredentials() ?: getLocalhostCredentials()
}

fun getLocalhostCredentials(): DbCredentials {
    return DbCredentials("mysql://localhost", "root", "mysqlpassword", "r_onliner_trends")
}