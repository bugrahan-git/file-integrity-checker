# File Integrity Checker

## Compile

`javac -XDignore.symbol.file -Xdiags:verbose *.java`

## Run

### createCert

`java ichecker createCert -c /path/to/file -k /path/to/file`

### createReg

`java ichecker createReg -l /path/to/logfile -h hashFunc -r /path/to/regFile -k /path/to/priKeyFile`



