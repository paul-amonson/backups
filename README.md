<!--
    Copyright (C) 2021 Paul Amonson)
    SPDX-License-Identifier: Apache-2.0
-->
# Backups
This is a simple command line Java-based tool used for backing up folder sets to a separate location. It supports the following features:

----

* Create a 256 bit key (___Keep it secret!!!___)
* Create a backup set from the commandline.
* Backup files using gzip then AES encryption using the 256 bit key.
* Restore files in place without overwriting newer files.
* Restore files to a different location (in the same tree form with the specied destination being the new root)
* If the backup set file is lost you can still manually recover the files using the index file stored with the backed up files.
  * You still need the key to do this so ___don't lose it___!
  * It will not restore to the original source location because the set file had that info.
* The index file for the backup set is also encrypted to hide filenames.
* Good fit for pre-internet compression/encryption to preserve "Trust No One". Use with any cloud service exposed as a local filesystem.

This was tested using Java 11.0.13 (and I am continuing to use it) but should work with any newer version as well.

Feel free to file bugs on github.

Thanks,
Paul
