package com.priyank.drdelivery.shipmentDetails.domain

import android.util.Log
import com.google.api.client.util.Base64
import com.google.api.client.util.StringUtils
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.priyank.drdelivery.shipmentDetails.domain.model.InterestingEmail
import java.util.Date

class ParseEmail {

    // #TODO
    // Add more pattern to find link from Amazon,Myntra,Ajio etc
    // Also refactor the code to make so that new pattern can be added easily in future...

    private fun getTextFromBodyPart(bodyPart: MessagePart): String {
        var result = ""
        if (bodyPart.mimeType == "text/plain" || bodyPart.mimeType == "text/html") {
            val data = bodyPart.body.data
            result = StringUtils.newStringUtf8(Base64.decodeBase64(data))
        }
        return result
    }

    fun parseEmail(email: Message): InterestingEmail? {
        var parsedEmail = ""

        if (email.payload.parts != null) {
            for (part in email.payload.parts) {
                parsedEmail += getTextFromBodyPart(part)
            }
        } else {
            parsedEmail += getTextFromBodyPart(email.payload)
        }

        val flipkartPattern = Regex("http://delivery\\.ncp\\.flipkart\\.com/[A-Za-z0-9?=&/\\\\+]+")
        val matchResult = flipkartPattern.find(parsedEmail)

        return if (matchResult != null) {
            val foundLink = matchResult.value
            Log.d("EMAIL ${timeEmailWasReceived(email)}", parsedEmail)
            InterestingEmail(
                email.id,
                foundLink,
                "Flipkart",
                timeEmailWasReceived(email),
                null
            )
        } else {
            Log.d("LINK NOT FOUND IN EMAIL ${timeEmailWasReceived(email)}", parsedEmail)
            null
        }
    }

    private fun timeEmailWasReceived(email: Message): String {
        val timeInMillis = email["internalDate"] as String
        val netDate = Date(timeInMillis.toLong()).toLocaleString()
        return netDate
    }
}
