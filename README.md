# password-updater
Update passwords in different files.

## Usage
Update the `config.txt` with the desired locations.

Run the `main`-method and enter the old and new password.   
When clicking `update` the old password will be replaced by the new password in the specified files.

## Base64 passwords
The updater will search for both plain text as base64-passwords.   
The base64 logic will search for `username:password`.   
To update the username you need to change the constant `USERNAME` in `Main.kt` to the desired value.
