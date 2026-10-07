package com.swiftyspiffy.burkeblackapp.auth
import org.junit.Assert.*
import org.junit.Test
class OAuthAttemptTest {
 @Test fun randomNoncesAndStrictLifetime() {
  val nonce=OAuthAttempt.nonce()
  assertTrue(nonce.matches(Regex("[0-9a-f]{64}")))
  assertNotEquals(nonce,OAuthAttempt.nonce())
  assertTrue(OAuthAttempt.matches(nonce,listOf(nonce),1000,1001))
  assertFalse(OAuthAttempt.matches(nonce,listOf(nonce),1000,601000))
  assertFalse(OAuthAttempt.matches(nonce,listOf(nonce),1000,999))
  assertFalse(OAuthAttempt.matches(null,listOf(nonce),1000,1001))
  assertFalse(OAuthAttempt.matches(nonce,emptyList(),1000,1001))
  assertFalse(OAuthAttempt.matches(nonce,listOf(nonce,nonce),1000,1001))
  assertFalse(OAuthAttempt.matches(nonce,listOf("bad"),1000,1001))
 }
}
