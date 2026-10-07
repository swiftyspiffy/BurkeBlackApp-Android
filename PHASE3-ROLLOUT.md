# API migration client preparation

The `goApiAuth` Gradle property defaults to false. Enable a private build with
`-PgoApiAuth=true` only after the Go start/callback and POST renew/refresh routes
are available. No network failure automatically downgrades to the older protocol.
Current release builds retain PHP login/refresh behavior.

The enabled protocol stores a random, ten-minute nonce in app-private preferences,
checks the exact callback scheme/host and unique fields, consumes state before
accepting success or error, explicitly renews active API sessions, and refreshes
Twitch tokens using POST. Expired/mismatched/replayed state is rejected. Callback
URI and HTTP-body logging are removed; only method/path diagnostics remain.

CI compiles both flag values with synthetic tests, without production Firebase
configuration. These builds are not distributed. Before release, use a properly
configured installed build to check browser cookies, cancellation, account switch,
process death during sign-in, replay/mismatched callbacks, session renewal,
revocation, temporary errors, GIF search and Twitch-token consumers. Keep the last
verified Go auth image for rollback; PHP has no strict start-route equivalent.
