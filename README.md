
# Extract File/Folder Changes EFC

## Why?
First of all I wanted to try Java, because this is my first Java program :-)

## How?

This small tool should be very useful when you want to create an extract from a folder structure.  
The base of extract could be
1. a date/time: every file/folder which has been created or modified after this time will be in the extract
2. an other folder structure: every file/folder which does not exists in the other folder structure or has been modified later than the reference file, will be in the extract.

At the end the EFC will create a folder structure with copy of the files what extracted from the source folder structure in accordance with one of the rules above.

## Parameters

1. Root of source folder structure
2. Root of target folder structure
3. A date in **yyyy.mm.dd** format or the **root of the reference folder structure**
4. Optional time in **hh24:mi:ss** format. Default is 00:00:00


## Examples

Imagine that we collect and catalogue pictures in C:\Pictures folder structure. Sometimes we want to create a backup to E:\Pictures, but only from the changes.
The EFC can do it, but in a middle step it creates a folder structure with these "Newer" files and does not write over directly the target. It allows us to check them before copying into and overwriting the target.

Create a copy from each file (with folders if necessary)/folder what has created or modified after 2018.01.01 17:00:00. It will create a d:\temp\...  structure. 

    EFC c:\pictures d:\temp 2018.01.01 17:00:00

Create a copy from each file (with folders if necessary)/folder what does not exists, or newer then the target (reference).

    EFC c:\pictures d:\temp e:\pictures 



