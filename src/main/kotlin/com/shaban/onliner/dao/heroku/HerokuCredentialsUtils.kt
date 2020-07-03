package com.shaban.onliner.dao.heroku

import com.shaban.onliner.dao.DbCredentials

fun getHerokuJawsDbMariaCredentials(): DbCredentials? {
    val credString = System.getenv("JAWSDB_MARIA_URL") ?: return null
    return parseHerokuCredentialString(credString)
}

/**
credString - credential line with next format:  mysql://<username>:<password>@<host>/<dbname>
 */
private fun parseHerokuCredentialString(credString: String): DbCredentials {

    val regex = Regex("mysql://(.+):(.+)@(.+)/(.+)")
    val matchResult = regex.matchEntire(credString)
            ?: throw  IllegalArgumentException("Can not parse credential string for jawsdb maria from environment variable 'JAWSDB_MARIA_URL'")
    val groupValues = matchResult.groupValues
    return DbCredentials("mysql://" + groupValues[3], groupValues[1], groupValues[2], groupValues[4])
}