<p align="center">
  <img src="https://github.com/tlm9201/PackStacker/assets/69724732/b27f942c-0c9a-40fb-a035-79fd88e19a28" />
</p>

# PackStacker
PackStacker is a simple resource management plugin intended for "stacking" packs on minecraft clients.
Downloads can be found at [releases](https://github.com/tlm9201/PackStacker/releases).

## Short Guide
Resource packs are defined through ".pack" files. Navigate to `PackStack/packs/` under your servers plugins folder.

To define a pack. Create a file with the extension `.pack`. (i.e `example.pack`)
Pack files follow yaml syntax. See below for an example `.pack` file.

```yaml
name: "MvndiPack"
url: "https://github.com/HydrolienF/MvndiPack/releases/download/1.20.6.425/MvndiPack.zip"
hash: "D64DCFA57B5A94383FA39F32BE3B6D38D853304C"
priority: 0
required: true
prompt: "Hello <rainbow>world</rainbow>."
load_on_join: true
```

### Required Pack Options
* `name` The name of the pack. No two packs can have the same name. Pack names *MUST* be unique.
* `url` The url to the pack. This url must be a direct download link. Typically, these links end in the file format `.zip`.
* `hash` The SHA1 hash of the pack. You can obtain this by running `certutil -hashfile "filename.exe" SHA1` on Windows.

### Optional Pack Options
* `priority` A number value corresponding to which order this pack will manifest. A lower number means the pack will be on a "lower" level on the client. Default: 0 (no priority)
* `required` Whether or not to kick the client if the player refuses the pack. Default: false
* `prompt` A MiniMessage compatible string that users will be prompted with when a pack is requested to the client. Default: no prompt (null)
* `load_on_join` Whether or not to prompt the user this pack on join. Default: false

### Commands
* `/pack` The core PackStacker command.
* `/pack load <packName>` Loads the specified resource pack on the player running this command. Permission: `pack.load.self`
* `/pack load <packName> <username>` Loads a resource pack on the specified online player. Permission: `pack.load.others`

## License
PackStacker is licensed under the [GNU AFFERO GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/agpl-3.0.en.html).
