# fabric-bungee-forwarding

Fabric mod for Minecraft 1.21.10 that implements BungeeCord-style forwarding on the server side.

## Behaviour
- Accepts only logins that include the Bungee/Velocity legacy forwarding payload (`host\0ip\0uuid\0profileJson`).
- Rejects plain/offline logins with a disconnect message before authentication proceeds.
- Rewrites the connection address to the forwarded IP and applies forwarded UUID/properties to the joining profile.
- Increases the handshake hostname length limit so the forwarded payload is not truncated.

## Building
```bash
./gradlew build
```
The remapped jar is produced at `build/libs/fabric-bungee-forwarding-<version>.jar`.

## Deploying
1. Remove FabricProxy-Lite (or any other forwarding mod) from the server `mods/` directory.
2. Drop the built jar into `mods/`.
3. Set your proxy to `player-info-forwarding-mode = legacy` (Velocity) and ensure backends run in offline mode with `bungeecord: true`.
4. Restart the server.
