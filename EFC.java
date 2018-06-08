import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/*

This small tool should be very useful when you want to create an extract from a folder structure.
The base of extract could be
1. a date/time: every file/folder which has been created or modified after this time will be in the extract
2. an other folder structure: every file/folder which does not exists in the other folder structure or has been modified later than the reference file, will be in the extract.

At the end the EFC will create a folder structure with copy of the files what extracted from the source folder structure in accordance with one of the rules above.

Parameters
1. Root of source folder structure
2. Root of target folder structure
3. A date in yyyy.mm.dd format or the root of the reference folder structure
4. Optional time in hh24:mi:ss format. Default is 00:00:00

Examples
Imagine that we collect and catalogue pictures in C:\Pictures folder structure. Sometimes we want to create a backup to E:\Pictures, but only from the changes.
The EFC can do it, but in a middle step it creates a folder structure with these "Newer" files and does not write over directly the target. It allows us to check them before copying into and overwriting the target.
Create a copy from each file (with folders if necessary)/folder what has created or modified after 2018.01.01 17:00:00. It will create a d:\temp\...  structure.
    EFC c:\pictures d:\temp 2018.01.01 17:00:00
Create a copy from each file (with folders if necessary)/folder what does not exists, or newer then the target (reference).
    EFC c:\pictures d:\temp e:\pictures

    History of changes
    yyyy.mm.dd | Version | Author         | Changes
    -----------+---------+----------------+-------------------------
    2018.06.07 |  1.0    | Ferenc Toth    | Created

*/
public class EFC {

    public static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    public static String     bs         = "\\";
    String  source;
    String  target;
    String  reference;
    String  date;
    String  time;
    Date    refDate;
    Date    minDate;

    /* ***********************************************************************************************
       append \ if there was not.
    *********************************************************************************************** */
    public String SetFolder( String folder ) {
        if ( ! folder.substring( folder.length() - 1 ).equals( bs ) ) folder = folder + bs;
        return folder;
    }

    /* ***********************************************************************************************
       returns "" if the arg is not date otherwise the returns with arg
    *********************************************************************************************** */
    public String SetDate( String dateString ) {
        try {
            Date   testDate = dateFormat.parse( dateString + " 00:00:00" );
            return dateString;
        } catch ( Exception e ) {
            return "";
        }
    }

    /* ***********************************************************************************************
       returns "00:00:00" if the arg is not time otherwise the returns with arg
    *********************************************************************************************** */
    public String SetTime( String timeString ) {
        try {
            Date   testDate = dateFormat.parse("2010.01.01 " + timeString );
            return timeString;
        } catch ( Exception e ) {
            return "00:00:00";
        }
    }

    /* ***********************************************************************************************
        return minDate if the file does not exist or the greater of crea, mod date
    *********************************************************************************************** */
    public Date fileDate ( String fullPath ) throws Exception {

        if ( ! new File( fullPath ).exists() ) return minDate;

        Path file = Paths.get( fullPath );
        BasicFileAttributes attr = Files.readAttributes( file, BasicFileAttributes.class );
        if ( attr.lastModifiedTime().toMillis() > attr.creationTime().toMillis() ) 
            return  new Date( attr.lastModifiedTime().toMillis() ) ;

        return  new Date( attr.creationTime().toMillis() );
    }

    /* ***********************************************************************************************
        Go!
    *********************************************************************************************** */
    public void Go () throws Exception {

        System.out.println("Source    :" + source );
        System.out.println("Target    :" + target );
        if ( ! date.equals( "" ) ) {
            refDate = dateFormat.parse( date + " " + time );
            System.out.println("Date      :" + date + " " + time );
        } else {
            System.out.println("Reference :" + reference );
        }
        System.out.println("======================================================================");

        search ( new File( source ) );

    }

    /* ***********************************************************************************************
        Recursive Search
    *********************************************************************************************** */
    private void search(File file)  throws Exception {

        Date     actDate;
        String   actSource;
        String   actTarget;
        String   actTargetDir;
        String   actReference;

        if ( file.isDirectory() ) {

            //  System.out.println("Searching directory ... " + file.getAbsoluteFile());

            //do you have permission to read this directory?
            if ( file.canRead() ) {

                for ( File temp : file.listFiles() ) {

                    if ( temp.isDirectory() ) { search( temp ); }
                    else {

                        actSource    = temp.getAbsoluteFile().toString();
                        actTarget    = target + temp.getAbsoluteFile().toString().substring( source.length() );
                        actTargetDir = actTarget.substring( 0, actTarget.length() - temp.getName().length() );

                        if ( ! date.equals( "" ) ) { actDate = refDate; }
                        else {

                            actReference = reference + temp.getAbsoluteFile().toString().substring( source.length() );
                            actDate = fileDate ( actReference );
                        }
                        if ( fileDate( actSource ).compareTo( actDate ) > 0 ) {

                            System.out.println( temp.getAbsoluteFile().toString() + " => " + actTarget );

                            if (! new File(actTargetDir).exists()) new File(actTargetDir).mkdirs();

                            Files.copy(temp.toPath(), new File(actTarget).toPath(),  StandardCopyOption.REPLACE_EXISTING );

                        }
                    }
                }
            } else {

                System.out.println( file.getAbsoluteFile() + "Permission Denied" );

            }
        }

    }



    /* ***********************************************************************************************
       Main entry point
    *********************************************************************************************** */
    public static void main( String[] args ) throws Exception {

        EFC  efc = new EFC();
        efc.minDate = dateFormat.parse( "1900.01.01 00:00:00" );

        if ( args.length > 2 && args.length < 5 ) {

            efc.source    = efc.SetFolder( args[0] );
            efc.target    = efc.SetFolder( args[1] );
            efc.date      = efc.SetDate  ( args[2] );
            if ( ! efc.date.equals( "" ) ) { efc.reference = ""; } else { efc.reference = efc.SetFolder( args[2] ); }
            if ( args.length ==  4 ) { efc.time = efc.SetTime( args[3] ); } else { efc.time = "00:00:00"; }

            if ( ! new File( efc.source ).exists() ) { System.out.println( "The Source folder does not exists: " + args[0] ); }
            else
            if ( ! new File( efc.target ).exists() ) { System.out.println( "The Target folder does not exists: " + args[1] ); }
            else
            if ( ! efc.reference.equals( "" ) && ! new File( efc.reference ).exists() ) { System.out.println( "The Reference folder does not exists: " + args[2] ); }
            else
            if ( ! efc.reference.equals( "" ) || ! efc.date.equals( "" ) ) { efc.Go(); }

        }
        else {
            System.out.println( "The right parameters for calling EFC:" );
            System.out.println( "-------------------------------------" );
            System.out.println( "1. Root of Source folder structure" );
            System.out.println( "2. Root of Target folder structure" );
            System.out.println( "3. A date in yyyy.mm.dd format or the root of the reference folder structure" );
            System.out.println( "4. Optional time in hh24:mi:ss format. Default is 00:00:00" );
            System.out.println( " " );
            System.out.println( "Examples:" );
            System.out.println( "---------" );
            System.out.println( "Create a copy from each file (with folders if necessary)/folder what has created or modified after 2018.01.01 17:00:00. It will create a d:\\temp\\...  structure. " );
            System.out.println( " " );
            System.out.println( "EFC c:\\pictures d:\\temp 2018.01.01 17:00:00" );
            System.out.println( " " );
            System.out.println( "Create a copy from each file (with folders if necessary)/folder what does not exists, or newer then the target (reference)." );
            System.out.println( "" );
            System.out.println( "EFC c:\\pictures d:\\temp e:\\pictures" );
        }

    }

}

