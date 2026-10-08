# API protocol builds

Builds use Go authentication and app API routing by default. The protocol uses
`/app/auth/start`, the stateful callback, and POST `/app/auth/renew` and
`/app/twitch-token`. Updated requests carry the Go routing marker only to the
HTTPS app API. Other hosts are unchanged.

Use `-PgoApiAuth=false` for an explicit legacy compatibility build, or
`-PgoApiAuth=true` to select Go explicitly. Network failures never downgrade
the protocol. Disabling the property affects newly built apps only.

The Go protocol stores a random, ten-minute nonce in app-private preferences,
checks the exact callback scheme/host and unique fields, consumes state before
accepting success or error, explicitly renews active API sessions, and refreshes
Twitch tokens using POST. Expired/mismatched/replayed state is rejected. Callback
URI and HTTP-body logging are removed; only method/path diagnostics remain.

CI tests the unmodified default and both explicit property values with synthetic
Firebase configuration. It builds release APKs and checks the generated debug
and release protocol settings. CI artifacts are not signed for distribution.

For a distributed build, supply the private Firebase configuration and existing
release signing configuration outside source control. Check installed sign-in,
cancellation, account switching, session persistence, temporary errors, GIF
search and Twitch-token consumers. A backend rollback must retain the secure Go
start/callback and POST renewal/refresh routes for already-installed Go clients;
PHP does not implement the strict start route.
