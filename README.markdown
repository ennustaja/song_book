Song book application
==============
---

To fix a song typo or modify the songs
---------------
* Open the song database file with some Sqlite browser:

    ``$ sqlitebrowser assets/songbook_database &``

* Change the songs and save the database file.
* Remove the old split database files:

    ``$ rm assets/split_db*``

* Split the updated database into separate files:

    ``$ split songbook_database -b 1048576 split_db_``

* This should create files split\_db\_aa and split\_db\_ab and you are done. If it creates more files than these two then you need to edit the SongDbAdapter to load the other files as well. 

