package gr.agroknow.metadata.identifier;

import java.sql.Statement;
import java.io.* ;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.zettadata.simpleparser.*;



public class IdentityManager
{

        private SimpleLOM simpleLOM;
        private SimpleMetadata simpleMetadata;
        private SimpleMetadataFactory simpleMetadataFactory;
       
        private  int notfile=0;
        private  int countID = 1;
        private  int countURL = 1;

        private  HashMap<String,Integer> LoadedIds;
        private  HashMap<String,Integer> LoadedUrls;

        private double chkIdRes;
        private double chkUrlRes;

        private static IdentityManager instance = null ;


        private  List<IdentificationBean> iBeans ;
        // This is a list of identification beans as described in recommendation C.1
        // It is initiated from the DB by the constructor
        // It is updated each time a record should be added to the DB

        private List<IdentificationBean> newBeans ;
        // this is a list in which you can keep a copy of the identification beans
        // created by the algorithm (they duplicate the recorded added to the 1st list

       public static final String ROOT_FOLDER = "C:/Users/nimas/Documents/AGROKNOW/DeSolutions/LOM PARSER/lomParser/test" ;
       public static final String DUP_FOLDER = "C:/Users/nimas/Documents/AGROKNOW/DeSolutions/LOM PARSER/lomParser/Duplicate" ;
       public static final String SUSP_FOLDER = "C:/Users/nimas/Documents/AGROKNOW/DeSolutions/LOM PARSER/lomParser/Suspicious" ;
       
  //     public static final String LOM_FOLDER = ROOT_FOLDER + "lomtestset" + File.separator ;
       
      public static final String LOM_FOLDER = "C:/Users/nimas/Documents/AGROKNOW/DeSolutions/LOM PARSER/lomParser/LOM/TEST/lomtestset";
       
      public static final String DC_FOLDER = ROOT_FOLDER  + File.separator + "DCs" + File.separator ;
      public static final String NSDL_FOLDER = ROOT_FOLDER + File.separator + "NSDLs" + File.separator;

        protected IdentityManager(String dbURl) throws Exception
        {
        // In the constructor, we connect to the database, you read it and you use the data
        // to create the corresponding identification beans and put them in the list.

        
            iBeans = new ArrayList();
            newBeans = new ArrayList();
            LoadedIds = new HashMap<String,Integer>() ;
            LoadedUrls = new HashMap<String,Integer>();

            Set docIdSet = new HashSet<String>();
            Set<Integer> rsDocIds = new HashSet();
            int docId;

            Class.forName("com.mysql.jdbc.Driver");
          //  String dbUrl = "jdbc:mysql://127.0.0.1:3306/ID_SERVICE?user=root&password=admin";
            Connection con =  DriverManager.getConnection(dbURl);
            Statement stmt =  con.createStatement();
     
                String query = "select * from locations;";
                ResultSet rsUrls = stmt.executeQuery(query);


                while (rsUrls.next()){
                   //LoadedUrls.put(rsUrls.getString("data"), rsUrls.getString("id").hashCode());
                   rsDocIds.add(rsUrls.getInt("id"));
                                  
                }
              
           Iterator<Integer> docsIt = rsDocIds.iterator();
           
            while (docsIt.hasNext()){
                               
                docId = docsIt.next();
 
                query = "select data from identifiers where id='"+docId+"';";
                ResultSet rsIds = stmt.executeQuery(query);

                Set IdSet = new HashSet<String>();
                Set UrlSet = new HashSet<String>();

                while (rsIds.next()){
                    IdSet.add(rsIds.getString("data"));
                    LoadedIds.put(rsIds.getString("data"),docId );
                  // System.out.println(docId+ ":" + rsIds.getString("data"));
                }

                query = "select data from locations where id='"+docId+"';";
                rsUrls = stmt.executeQuery(query);

                while (rsUrls.next()){
                    UrlSet.add(rsUrls.getString("data"));
                    LoadedUrls.put(rsUrls.getString("data"), docId);
                }
                
                IdentificationBean idBean= new IdentificationBean();
                idBean.setdocId(docId);
                idBean.setIdentifiers(IdSet);
                idBean.setLocations(UrlSet);
                iBeans.add(idBean);
            }        

            System.out.println( "iBeans size:" + iBeans.size());

        }

        public static IdentityManager getInstance(String dbURl) throws IdentifierException, Exception
        {
                if (instance == null)
                {
                    synchronized(IdentityManager.class)
                    {
                        if (instance == null)
                        {
                            instance = new IdentityManager(dbURl) ;
                        }
                    }
                }
                return instance ;
        }


        public void saveNewRecords(String dbUrl) throws Exception
        {
        // With this method, we save the content of the newBeans list into the database
           
            Class.forName("com.mysql.jdbc.Driver");
           // String dbUrl = "jdbc:mysql://127.0.0.1:3306/ID_SERVICE?user=root&password=admin";
            Connection con =  DriverManager.getConnection(dbUrl);
            Statement stmt =  con.createStatement();

            System.out.println("newBeans Size:" + newBeans.size());
            Iterator<IdentificationBean> beansIt = newBeans.iterator();


            while (beansIt.hasNext())
             {
                 IdentificationBean idBean = beansIt.next();
                 Iterator it = idBean.getIdentifiers().iterator();
                 Iterator it2 = idBean.getLocations().iterator();

                 int docIdentifier = idBean.getdocId();                 

                 while (it.hasNext()){
                             try
                             {

                             String str = it.next().toString();

                            //  if ( LoadedIds.containsKey(str) == false)
                            //  {
                                String query = "INSERT INTO identifiers (id, data) VALUES ("+ docIdentifier +", '"+ str +"') ";
                                stmt.executeUpdate(query);                           
                              //   }
                              }
                             catch (SQLException e)
                              {
                                 System.out.println("error during insertion in DB:" + e );
                              }
                            }

                       while (it2.hasNext()){
                            String location = it2.next().toString();
                           try
                             {
                         //       if ( LoadedUrls.containsKey(location) == false)
                         //       {
                                String query = "INSERT INTO locations (id, data) VALUES ("+ docIdentifier +", '"+ location +"') ";
                                stmt.executeUpdate(query);
                                 }
                         //     }
                             catch (SQLException e)
                              {
                              }
                            }
             }

        }
     

      public SimpleMetadata extrLOM(String pathToMetadata ) throws ParserException, net.zettadata.simpleparser.ParserException
	{
		//File metadataFile = new File( pathToMetadata ) ;

		simpleMetadata = SimpleMetadataFactory.getSimpleMetadata(SimpleMetadataFactory.LOM ) ;
		simpleMetadata.load(pathToMetadata);

                return simpleMetadata;
	}


    public SimpleMetadata extrDC(String pathToMetadata) throws ParserException, net.zettadata.simpleparser.ParserException
	{
		//File metadataFile = new File( pathToMetadata ) ;

		simpleMetadata = SimpleMetadataFactory.getSimpleMetadata( SimpleMetadataFactory.DC ) ;
		simpleMetadata.load(pathToMetadata);

                return simpleMetadata;
	}
    
     public SimpleMetadata extrNSDL(String pathToMetadata) throws ParserException, net.zettadata.simpleparser.ParserException
	{
		//File metadataFile = new File( pathToMetadata ) ;

		simpleMetadata = SimpleMetadataFactory.getSimpleMetadata( SimpleMetadataFactory.NSDL ) ;
		simpleMetadata.load(pathToMetadata);

                return simpleMetadata;
	}


    public void categorize(String pathToMeta, String Folder, String duplFolder, String suspFolder, String MetadataFormat) throws SQLException, IOException, net.zettadata.simpleparser.ParserException{
//This method compares the set of extracted identifiers and locations from each xml file existing in the "pathToMeta" directory
//with the ones loaded from the database. There are 3 different cases (not found,duplicates,grey)       
       
            String files;
            String setName = Folder;
           
            File folder = new File(pathToMeta);         
            File targetDir = new File(Folder);
                      
            
            File SDir = new File(Folder + File.separator+ MetadataFormat);           
            if (!SDir.exists())           
            {
                SDir.mkdir();
            }
                   
            File FOUND = new File(duplFolder);
            if (!FOUND.exists())           
                  {
                      FOUND.mkdir();
                  }
      
            File GREY = new File(suspFolder);
            if (!GREY.exists())           
                  {
                      GREY.mkdir();
                  }
          
            WeightedIdentifier decision;            
                                       

            List<File> listOfFiles = new ArrayList();
            
            File[] list = folder.listFiles();
            // File child : folder.listFiles().length
                                                                 
            /*   for (int i=0;i<list.length; i++) {
                         
                 if (list[i].isDirectory())
                      listOfFiles.addAll(Arrays.asList(list[i].listFiles()));
                  else
                       listOfFiles.add(list[i]);
             }*/
           
            
            for (File child : folder.listFiles()) {
                
                  if (child.isDirectory()){
                      
                      setName = child.getName();
                      System.out.println(setName);
                      targetDir = new File(SDir + File.separator+setName) ;
                      
                      if(!targetDir.exists())
                      {
                          targetDir.mkdir();
                      }
                       
                       listOfFiles.addAll(Arrays.asList(child.listFiles()));
                       
                       //   SimpleLOM elements ;
                        SimpleMetadata elements;

                        if (!LoadedIds.isEmpty())
                        {
                          countID = LoadedIds.size();
                          countURL = LoadedUrls.size();
                        }

                        try {                

                            for (File file:listOfFiles)
                            {

                               if (file.isFile() && file.getAbsolutePath().contains(".xml"))
                               {
                               
                               files = file.getName();

                               try
                                {
                                  //System.out.println("file path is:" + file.getAbsolutePath());  
                               
                                elements = null;    
                                if ("LOM".equals(MetadataFormat)) {
                                         elements = instance.extrLOM( file.getAbsolutePath() ) ;                                         
                                     }
                                else if ("DC".equals(MetadataFormat)) {
                                         elements = instance.extrDC( file.getAbsolutePath() ) ;                                      
                                     }   
                                else if ("NSDL".equals(MetadataFormat)) {
                                         elements = instance.extrNSDL(file.getAbsolutePath()) ;                                           
                                     }
                                 else {
                                         System.out.println("The metadata format is not supported");
                                     }
                                                                                                      
                                   Set<String> DocId = new HashSet(elements.getIdentifiers());
                                   Set<String> DocUrl = new HashSet (elements.getLocations());
                                   Set<String> newDocIds = new HashSet();
                                   Set<String> newDocUrls = new HashSet ();

                                      decision = decisionMaking(DocId, DocUrl);

                              /* 3 CASES for which the records should be copied at NOT_FOUND folder and their identifiers and
                               locations inserted in the DB*/
                                    if (decision.getWeight() == 0)
                                    {
                                       // System.out.println("pass CASE");
                                        //File copied = new File(NOT_FOUND);
                                        //countFiles(file ,copied, countID);
                                       // 9/1 editCopyFiles(file, NOT_FOUND,countID);
                                        editCopyFiles(file,targetDir,decision.getDocId());


                                        Iterator it = DocId.iterator();
                                        Iterator it2 = DocUrl.iterator();

                                        while (it.hasNext()){
                                              String identifier = it.next().toString();
                                              if (LoadedIds.containsKey(identifier) == false){
                                              // LoadedIdSet.add(identifier);
                                              LoadedIds.put(identifier,decision.getDocId());
                                              newDocIds.add(identifier);
                                              }

                                        }

                                         while (it2.hasNext()){
                                                 String location = it2.next().toString();
                                                 if (LoadedUrls.containsKey(location) == false){
                                                     LoadedUrls.put(location,decision.getDocId());
                                                     newDocUrls.add(location);
                                                 }

                                        }

                                       IdentificationBean nBean = new IdentificationBean();
                                       nBean.setdocId(decision.getDocId());
                                       nBean.setIdentifiers(newDocIds);
                                       nBean.setLocations(newDocUrls);
                                       newBeans.add(nBean);

                                       countID ++;
                                       countURL ++;
                                          }
                                  /*2 CASES for which the records should be rejected*/
                                   else if (decision.getWeight()==1.0)
                                  {
                                            //System.out.println("REJECT CASE");
                                            //File copied = new File(FOUND);
                                             copyInitFile(file,FOUND);    
                                             copyFiles(file,FOUND, decision.getDocId());
                                  }
                                  /* Grey area case*/
                                    else if (decision.getWeight() == 0.5)
                                    {
                                             //System.out.println("GREY CASE");
                                        copyInitFile(file,GREY);      
                                        copyFiles(file ,GREY, decision.getDocId());

                                             // copyFiles(file ,GREY, decision.getDocId());

                                    }

                                }
                                        catch (ParserException e)
                                        {
                                                e.printStackTrace() ;
                                        }

                                }
                            



                                     }         

                                    }catch(ClassNotFoundException e) {
                                   e.printStackTrace();
                                   }

                       
                 /* }
                  else
                       listOfFiles.add(child);*/
                        }
            
          
            System.out.println("SIZE OF CREATED BEANS IS:"+ newBeans.size() + "countID:"+countID);
            
            if (!listOfFiles.isEmpty()){    
                listOfFiles.removeAll(Arrays.asList(child.listFiles())); }
            
         }                    
    }

public void categorizeFolder(String pathToMeta, String Folder, String duplFolder, String suspFolder, String MetadataFormat) throws SQLException, IOException, net.zettadata.simpleparser.ParserException{
//This method compares the set of extracted identifiers and locations from each xml file existing in the "pathToMeta" directory
//with the ones loaded from the database. There are 3 different cases (not found,duplicates,grey)       
       
            String files;
            String setName = Folder;
           
            File folder = new File(pathToMeta);         
            File targetDir = new File(Folder);
            if (!targetDir.exists())           
                  {
                      targetDir.mkdir();
                  }
                                          
                   
            File FOUND = new File(duplFolder);
            if (!FOUND.exists())           
                  {
                      FOUND.mkdir();
                  }
      
            File GREY = new File(suspFolder);
            if (!GREY.exists())           
                  {
                      GREY.mkdir();
                  }
          
            WeightedIdentifier decision;            
                                       

            List<File> listOfFiles = new ArrayList();
            
            File[] list = folder.listFiles();
            // File child : folder.listFiles().length
                                                                 
            /*   for (int i=0;i<list.length; i++) {
                         
                 if (list[i].isDirectory())
                      listOfFiles.addAll(Arrays.asList(list[i].listFiles()));
                  else
                       listOfFiles.add(list[i]);
             }*/
            
            
            
           
            
            for (File child : folder.listFiles()) {
                
                  if (child.isFile()){                                                              
                       
                       listOfFiles.add(child);
                  }
            }
                       
            //   SimpleLOM elements ;
             SimpleMetadata elements;

             if (!LoadedIds.isEmpty())
             {
               countID = LoadedIds.size();
               countURL = LoadedUrls.size();
             }

             try {                

                 for (File file:listOfFiles)
                 {

                    if (file.isFile() && file.getAbsolutePath().contains(".xml"))
                    {

                    files = file.getName();

                    try
                     {
                       //System.out.println("file path is:" + file.getAbsolutePath());  

                     elements = null;    
                     if ("LOM".equals(MetadataFormat)) {
                              elements = instance.extrLOM( file.getAbsolutePath() ) ;                                         
                          }
                     else if ("DC".equals(MetadataFormat)) {
                              elements = instance.extrDC( file.getAbsolutePath() ) ;                                      
                          }   
                     else if ("NSDL".equals(MetadataFormat)) {
                              elements = instance.extrNSDL(file.getAbsolutePath()) ;                                           
                          }
                      else {
                              System.out.println("The metadata format is not supported");
                          }

                        Set<String> DocId = new HashSet(elements.getIdentifiers());
                        Set<String> DocUrl = new HashSet (elements.getLocations());
                        Set<String> newDocIds = new HashSet();
                        Set<String> newDocUrls = new HashSet ();

                           decision = decisionMaking(DocId, DocUrl);

                   /* 3 CASES for which the records should be copied at NOT_FOUND folder and their identifiers and
                    locations inserted in the DB*/
                         if (decision.getWeight() == 0)
                         {
                            // System.out.println("pass CASE");
                             //File copied = new File(NOT_FOUND);
                             //countFiles(file ,copied, countID);
                            // 9/1 editCopyFiles(file, NOT_FOUND,countID);
                             editCopyFiles(file,targetDir,decision.getDocId());


                             Iterator it = DocId.iterator();
                             Iterator it2 = DocUrl.iterator();

                             while (it.hasNext()){
                                   String identifier = it.next().toString();
                                   if (LoadedIds.containsKey(identifier) == false){
                                   // LoadedIdSet.add(identifier);
                                   LoadedIds.put(identifier,decision.getDocId());
                                   newDocIds.add(identifier);
                                   }

                             }

                              while (it2.hasNext()){
                                      String location = it2.next().toString();
                                      if (LoadedUrls.containsKey(location) == false){
                                          LoadedUrls.put(location,decision.getDocId());
                                          newDocUrls.add(location);
                                      }

                             }

                            IdentificationBean nBean = new IdentificationBean();
                            nBean.setdocId(decision.getDocId());
                            nBean.setIdentifiers(newDocIds);
                            nBean.setLocations(newDocUrls);
                            newBeans.add(nBean);

                            countID ++;
                            countURL ++;
                               }
                       /*2 CASES for which the records should be rejected*/
                        else if (decision.getWeight()==1.0)
                       {
                                 //System.out.println("REJECT CASE");
                                 //File copied = new File(FOUND);
                                  copyInitFile(file,FOUND);    
                                  copyFiles(file,FOUND, decision.getDocId());
                       }
                       /* Grey area case*/
                         else if (decision.getWeight() == 0.5)
                         {
                                  //System.out.println("GREY CASE");
                             copyInitFile(file,GREY);      
                             copyFiles(file ,GREY, decision.getDocId());

                                  // copyFiles(file ,GREY, decision.getDocId());

                         }

                     }
                             catch (ParserException e)
                             {
                                     e.printStackTrace() ;
                             }

                     }

                          }         

                         }catch(ClassNotFoundException e) {
                        e.printStackTrace();
                        }
                       
                 System.out.println("SIZE OF CREATED BEANS IS:"+ newBeans.size() + "countID:"+countID);
                        }
            
                                                          
    
       public  double checkIdentifiers(Set Id1){
           Set resID = new HashSet(Id1);
           Set stID = new HashSet(LoadedIds.keySet());
           double vId=0;


           if ((stID.containsAll(resID)) || resID.containsAll(stID) || (resID==stID))
           {
               vId=1.0;
               return vId;
           }

           stID.retainAll(resID);
           if (stID.isEmpty()) // intersection of stID with Id1 is empty
           vId=0;
           else
           vId=0.5;

           return vId;
       }

       public double checkUrls(Set Url1){
           Set resUrl = new HashSet(Url1);
           Set stUrl=new HashSet(LoadedUrls.keySet());
           double vUrl=0;

           stUrl.retainAll(resUrl);
           if (stUrl.isEmpty())
           vUrl=0;
           else
           {
               vUrl=1.0;
               Iterator stUrlIT2 = stUrl.iterator();
               System.out.println("stUrl after intersection EXEI SIZE:"+stUrl.size());

               while (stUrlIT2.hasNext()){
                                   System.out.println("stUrl after intersection EXEI:"+stUrlIT2.next().toString());
                               }
           }
           return vUrl;
       }

       public WeightedIdentifier decisionMaking(Set DocId, Set DocUrl) throws ClassNotFoundException{

          WeightedIdentifier wId = new WeightedIdentifier();

          chkIdRes = checkIdentifiers(DocId);
          chkUrlRes = checkUrls(DocUrl);

        if (((chkIdRes==1)&&(chkUrlRes==0)) || ((chkIdRes==0.5)&&(chkUrlRes==0)) || ((chkIdRes==0)&&(chkUrlRes==0)))
        {
            wId.setWeight(0);
            wId.setDocId(countID);

            System.out.println( "TO INSERT");

        }
        else if (((chkIdRes==1.0)&&(chkUrlRes==1.0)) || ((chkIdRes==0.5)&&(chkUrlRes==1.0)))
        {
            wId.setWeight(1);
            Iterator it = DocId.iterator();
            //   System.out.println(" REJECTED WITH CHECKID="+checkIdentifiers(DocId));
            //   System.out.println("REJECTED WITH CHECKurls="+checkUrls(DocUrl));
        System.out.println( "TO REJECT");
            while (it.hasNext()) {
                String identifier = it.next().toString();

                if (LoadedIds.containsKey(identifier) == true)                  
                     wId.setDocId(LoadedIds.get(identifier));
                    
             }



    //in case of duplicate, it is returned the id of the existing object
        }
        else
        {
            wId.setWeight(0.5);
            System.out.println( "TO decide");
            Iterator it = DocUrl.iterator();
            //   System.out.println(" REJECTED WITH CHECKID="+checkIdentifiers(DocId));
            //   System.out.println("REJECTED WITH CHECKurls="+checkUrls(DocUrl));

            while (it.hasNext()) {
                String url = it.next().toString();

            if (LoadedUrls.containsKey(url) == true)
                wId.setDocId(LoadedUrls.get(url));
                                     
             }


        }
          return wId;
       }


      public static void writefile(String filename){
      try{
        Writer output = null;
        File file = new File(filename);
        output = new BufferedWriter(new FileWriter(file));


        output.close();
        System.out.println("File has been written");

        }catch(Exception e){
            System.out.println("Could not create file");
        }
    }

      public static void countFiles(File sourceFile , File targetLocation, int count)
              throws IOException{

        if (sourceFile.isFile()) {
           /* if (!targetLocation.exists()) {
               targetLocation.mkdir();
            }*/

                InputStream in = new FileInputStream(sourceFile);
                OutputStream out = new FileOutputStream(targetLocation+"/"+count+".xml");

                // Copy the bits from input stream to output stream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }

      /*
      public static void copyFiles(File sourceFile, File targetLocation, int docId)
              throws IOException{
   
        if (sourceFile.isFile()) {*/
      
           /* if (!targetLocation.exists()) {
               targetLocation.mkdir();
            }*/
     /*       
            File propFile = new File(targetLocation, sourceFile.getName()) ; 
            if (!propFile.exists()) {  
                 propFile.createNewFile();               
            }

                InputStream in = new FileInputStream(sourceFile);
            //    OutputStream out = new FileOutputStream(targetLocation+"/"+sourceFile.getName());
                OutputStream out = new FileOutputStream(targetLocation+"/"+docId +"_" +sourceFile.getName());

                // Copy the bits from input stream to output stream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
      */
      
      
 public static void copyInitFile( File sourceFile, File targetLocation)     
      {    
        try 
            {
                File propFile = new File(targetLocation, sourceFile.getName()) ; 
                if (!propFile.exists()) {  
                  propFile.createNewFile();               
                  
                  FileChannel source = null ;
                  FileChannel destination = null ;
                 

                try 
                {
                    source = new FileInputStream( sourceFile ).getChannel() ;
                    destination = new FileOutputStream( targetLocation + "/"+sourceFile.getName() ).getChannel() ;                    
                    destination.transferFrom( source, 0, source.size() ) ;
                }
                finally 
                {
                    if ( source != null ) 
                    {
                        source.close() ;
                    }
                    if ( destination != null ) 
                    {
                        destination.close() ;
                    }
                }
                  
                  
                  
                  
                  
                } 
            }
        catch ( IOException ioe ) 
        {
            System.err.println( "Cannot copy metadata record : " + ioe.getMessage() + " !" ) ;
        }
      }
      
      
      public static void copyFiles( File sourceFile, File targetLocation, int docId)     
      {
            try 
            {
                                                
                if( !targetLocation.exists() ) 
                {
                    targetLocation.createNewFile() ;
                }

                FileChannel source = null ;
                FileChannel destination = null ;
                 

                try 
                {
                    source = new FileInputStream( sourceFile ).getChannel() ;
                    destination = new FileOutputStream( targetLocation + "/"+docId +"_" +sourceFile.getName() ).getChannel() ;                    
                    destination.transferFrom( source, 0, source.size() ) ;
                }
                finally 
                {
                    if ( source != null ) 
                    {
                        source.close() ;
                    }
                    if ( destination != null ) 
                    {
                        destination.close() ;
                    }
                }
        }
            catch ( IOException ioe ) 
            {
            System.err.println( "Cannot copy metadata record : " + ioe.getMessage() + " !" ) ;
        }
}


      


  public static void editCopyFiles(File sourceFile , File targetLocation, int docId)
              throws IOException{

        if (sourceFile.isFile()) {
            if (!targetLocation.exists()) {
               targetLocation.mkdir();
            }

            String line;
            Integer id = docId;
            
                InputStream in = new FileInputStream(sourceFile);
                StringBuilder sb = new StringBuilder() ;
                BufferedReader buffer = new BufferedReader(new FileReader(sourceFile));

                while ((line=buffer.readLine())!=null)
                     sb.append(line+"\n");

                String xmlFile = sb.toString();
                xmlFile = xmlFile.replaceFirst("<identifier>","<identifier><catalog>AK</catalog><entry>"+ docId +"</entry></identifier><identifier>");


                //System.out.println(xmlFile);

                File edited = new File(targetLocation, id.toString()+".xml");
                PrintWriter pw = new PrintWriter(new FileOutputStream(edited));
                pw.write(xmlFile);
                pw.close();
            }
        }

    
 /*    public static void main( String[] args ) throws Exception
	{       
          String dbUrl = "jdbc:mysql://127.0.0.1:3306/id_SERVICE?user=root&password=admin";
          IdentityManager_v1.getInstance(dbUrl).categorizeFolder(LOM_FOLDER,ROOT_FOLDER,DUP_FOLDER,SUSP_FOLDER,"LOM");
          IdentityManager_v1.getInstance(dbUrl).saveNewRecords(dbUrl);
	}*/
  
    
       public static void main( String[] args ) throws Exception
	{
     
                          
         if (args.length != 6) {
                System.err.println("Usage: java IdentityManager param0(metadata folder) param1(target) param2(dup folder) param3(susp folder) param4(metadata format) param5(dbURL)");
                System.exit(1);
            }
        long startTime = System.currentTimeMillis();
         
        IdentityManager.getInstance(args[5]).categorizeFolder(args[0],args[1],args[2],args[3],args[4]);
        IdentityManager.getInstance(args[5]).saveNewRecords(args[5]);
     
         long endTime   = System.currentTimeMillis();
         long totalTime = endTime - startTime;
         System.out.println(totalTime);
	}

}