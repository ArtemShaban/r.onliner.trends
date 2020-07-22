package com.shaban.onliner.dao

fun getDbCredentials(): DbCredentials {
    return getAmazonRdsMariaDbCredentials()
            ?: getHerokuJawsDbMariaCredentials()
            ?: getLocalhostCredentials()
}

fun getHerokuJawsDbMariaCredentials(): DbCredentials? {
    val credString = System.getenv("JAWSDB_MARIA_URL") ?: return null
    return parseDbCredentialString(credString)
}

fun getAmazonRdsMariaDbCredentials(): DbCredentials? {
    val credString = System.getenv("AMAZON_MARIA_DB_URL") ?: return null
    return parseDbCredentialString(credString)
}

fun getLocalhostCredentials(): DbCredentials {
    return DbCredentials("mysql://localhost", "root", "mysqlpassword", "r_onliner_trends")
}

/**
credString - credential line with next format:  mysql://<username>:<password>@<host>/<dbname>
 */
private fun parseDbCredentialString(credString: String): DbCredentials {

    val regex = Regex("mysql://(.+):(.+)@(.+)/(.+)")
    val matchResult = regex.matchEntire(credString)
            ?: throw  IllegalArgumentException("Can not parse credential string for jawsdb maria from environment variable 'JAWSDB_MARIA_URL'")
    val groupValues = matchResult.groupValues
    return DbCredentials("mysql://" + groupValues[3], groupValues[1], groupValues[2], groupValues[4])
}