P4
==

CS 3114 Project 4

Hash Table system for CS 3114 Project 4.

Group Members:
Chris Schweinhart (schwein)
Nate Kibler (nkibler7)

Project Description:
This project is a modification of the previous CS 3114 Project 3.
As such, the DatabaseManager and Handle classes are kept largely
the same, but the P4 main class has undergone several major changes.
The HashTable is completely new, to reflect the purpose of the
assignment.  In the HashTable, we keep track of entries by keeping
both handles for the sequence ID and the entry string in the table.
File parsing can be found in P4, and all memory management is in
DatabaseManager.