package com.codezone.mlutmod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.openide.DialogDescriptor;
import java.io.*;
import java.util.*;
import java.util.zip.*;

class MlutModStarter extends JPanel implements DocumentListener 
{ 
    private JTextField mRootDirectory; 
    private JComboBox mProjectName;
    private JTextField mMainFile;
    private JTextField mPackage;
    private DialogDescriptor desc; 
    private JPanel mPanel;
    private JComboBox mProjectType;
    
    private DirBrowseAction mDirBrowseAction;
    private GenerateAction mGenerateAction;
    
    private String mPackagePath;

    public MlutModStarter() 
    { 
        super(new BorderLayout()); 
        
        BorderLayout bl=new BorderLayout();
        mPanel=new JPanel(bl);

        JPanel pnlProject=new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets=new Insets(0, 0, 5, 5);
        
        c.gridy = 0;
        c.gridx = 0;
        c.weightx=0.3;
        pnlProject.add(new JLabel("Project Root Directory:"), c);
        c.gridx = 1;
        c.weightx=0.6;
        mRootDirectory = new JTextField(40); 
        mRootDirectory.setText("C:\\Program Files\\Pogamut 2\\PogamutPlatform\\projects");
        pnlProject.add(mRootDirectory, c);
        mDirBrowseAction=new DirBrowseAction("Browse", null, "Browse", 0, mRootDirectory);
        JButton btnDirBrowse=new JButton(mDirBrowseAction);
        c.weightx=0.1;
        pnlProject.add(btnDirBrowse);
        
        c.gridy = 1;
        c.gridx = 0;
        c.weightx=0.3;
        pnlProject.add(new JLabel("Project Name:"), c);
        c.gridx = 1;
        c.weightx=0.7;
        c.gridwidth=2;
        mProjectName=new JComboBox();
        populateSubDirs(mProjectName, "C:\\Program Files\\Pogamut 2\\PogamutPlatform\\projects");
        mProjectName.addItemListener(new ProjectItemListener());
        pnlProject.add(mProjectName, c);
        
        c.gridy = 2;
        c.gridx = 0;
        c.weightx=0.3;
        pnlProject.add(new JLabel("Package Name:"), c);
        c.gridx = 1;
        c.weightx=0.7;
        c.gridwidth=2;
        mPackage=new JTextField(40);
        pnlProject.add(mPackage, c);
        c.gridwidth=1;
        
        c.gridy = 3;
        c.gridx = 0;
        c.weightx=0.3;
        pnlProject.add(new JLabel("Main File:"), c);
        c.gridx = 1;
        c.weightx=0.7;
        c.gridwidth=2;
        mMainFile=new JTextField(40);
        
        if(mProjectName.getSelectedItem()==null)
        {
            mMainFile.setText("");
            return;
        }
        String proj_name=(String)mProjectName.getSelectedItem();
        populateMainFile("C:\\Program Files\\Pogamut 2\\PogamutPlatform\\projects", proj_name);
        
        pnlProject.add(mMainFile, c);
        c.gridwidth=1;
        
        c.gridy = 4;
        c.gridx = 0;
        c.weightx=0.3;
        pnlProject.add(new JLabel("CI Type:"), c);
        c.gridx = 1;
        c.weightx=0.6;
        mProjectType=new JComboBox();
        createMlutPackageFromZip(System.getProperty("user.dir")+"/Mlut");
        populateSubDirs(mProjectType, System.getProperty("user.dir")+"/Mlut");
        pnlProject.add(mProjectType, c);
        c.gridx=2;
        c.weightx=0.1;
        mGenerateAction=new GenerateAction("Generate", null, "Generate", 0);
        JButton btnGenerate=new JButton(mGenerateAction);
        pnlProject.add(btnGenerate, c);
        
        
        mPanel.add(pnlProject);
        mPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mPanel, BorderLayout.CENTER); 
    } 
    
    private class ProjectItemListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e) {
            if(mProjectName.getSelectedItem() != null)
            {
                String proj_name=(String)mProjectName.getSelectedItem();
                populateMainFile(mRootDirectory.getText(), proj_name);
            }
        }   
    }
    
    private void populateMainFile(String dirname, String proj_name)
    {
        String proj_path=dirname+"\\"+proj_name;
        File proj_dir=new File(proj_path);
        if(!proj_dir.exists())
        {
            mMainFile.setText("");
            return;
        }
        
        String src_path=proj_path+"\\src";
        File src_dir=new File(src_path);
        if(!src_dir.exists())
        {
            mMainFile.setText("");
            return;
        }
        
        ArrayList<File> packs=new ArrayList<File>();
        if(getMainFile(src_dir, packs))
        {
            String mainpath="";
            String package_path="";
            for(File pack : packs)
            {
                //JOptionPane.showMessageDialog(null, pack.toString());
                mainpath=pack.getName()+"\\"+mainpath;
                if(!pack.getName().equals("Main.java"))
                {
                    if(package_path.equals(""))
                    {
                        package_path=pack.getName();
                        mPackagePath=pack.getName();
                    }
                    else
                    {
                        package_path=pack.getName()+"."+package_path;
                        mPackagePath=pack.getName()+"\\"+package_path;
                    }
                }
            }
            mMainFile.setText(mainpath);
            mPackage.setText(package_path);
        }
    }
    
    private boolean getMainFile(File parent_dir, ArrayList<File> packs)
    {
        File[] dirs=parent_dir.listFiles();
        for(File dir: dirs)
        {
            if(dir.isDirectory())
            {
                if(getMainFile(dir, packs)==true)
                {
                    packs.add(dir);
                    return true;
                }
            }
            else
            {
                if(dir.getName().equals("Main.java"))
                {
                    packs.add(dir);
                    return true;
                }
            }
        }
        return false;
    }
    
    private void populateSubDirs(JComboBox combo, String dirname)
    {
        ArrayList<String> dirs=getDirectoryListing(dirname);
        combo.removeAllItems();
        for(String dir : dirs)
        {
            combo.addItem(dir);
        }
        if(dirs.size()>0)
        {
            combo.setSelectedIndex(0);
        }
    }
    
    private void createMlutPackageFromZip(String parent_dir)
    {
        int BUFFER=2048;
        try {
          BufferedOutputStream dest = null;
          final String resourcesPath = "packages.zip";
          InputStream stream = this.getClass().getResourceAsStream(resourcesPath);
          ZipInputStream zis = new ZipInputStream(stream);
          ZipEntry ze;
          while((ze=zis.getNextEntry())!=null){
            //JOptionPane.showMessageDialog(null, );
            String ds_path=parent_dir+"/"+ze.getName();
            //JOptionPane.showMessageDialog(null, ds_path);
            
            File ds_file=new File(ds_path);
            if(!ds_file.exists())
            {
                if(ds_path.endsWith(".java") || ds_path.endsWith(".jar"))
                {
                    String parent_path=ds_file.getParent();
                    File parent_fil=new File(parent_path);
                    parent_fil.mkdirs();
                    
                    int count;
                    byte data[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(parent_dir+"/"+ze.getName());            
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) 
                      != -1) {
                       dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    
                }
                else
                {
                    ds_file.mkdirs();
                }
            }
            
            zis.closeEntry();
          }

          zis.close();

        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    
    private ArrayList<String> getDirectoryListing(String dirname)
    {
        ArrayList<String> files=new ArrayList<String>();
        
        File dir = new File(dirname);
        if(!dir.exists())
        {
            return files;
        }

        File[] children =  dir.listFiles();
       
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else {
            for (int i=0; i<children.length; i++) {
                // Get filename of file or directory
                File cf = children[i];
                if(cf.isDirectory())
                {
                    files.add(cf.getName());
                }
            }
        }

        return files;
    }
    
    private class DirBrowseAction extends AbstractAction 
    {
        JTextField mTxtDir;
        public DirBrowseAction(String text, ImageIcon icon, String desc, Integer mnemonic, JTextField txtField) 
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
            mTxtDir=txtField;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            //displayResult("Action for first button/menu item", e);
            JFileChooser fc = new JFileChooser(); 
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            File f=new File(mTxtDir.getText());
            if(f.exists())
            {
                fc.setCurrentDirectory(f);
            }
            int returnVal = fc.showOpenDialog(null); 
            if(returnVal==JFileChooser.APPROVE_OPTION)
            {
                File file=fc.getSelectedFile();
                //JOptionPane.showMessageDialog(null, file.toString());
                mTxtDir.setText(file.toString());
                populateSubDirs(mProjectName, file.toString());
                
                if(mProjectName.getSelectedItem()==null)
                {
                    mMainFile.setText("");
                    return;
                }
                String proj_name=(String)mProjectName.getSelectedItem();
                populateMainFile(file.toString(), proj_name);
            }
        }
    }

    private class GenerateAction extends AbstractAction 
    {
        public GenerateAction(String text, ImageIcon icon, String desc, Integer mnemonic) 
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(mProjectType.getSelectedItem()==null)
            {
                JOptionPane.showMessageDialog(null, "Invalid CI Type");
                return;
            }
            if(mProjectName.getSelectedItem()==null)
            {
                JOptionPane.showMessageDialog(null, "Invalid Project Name");
                return;
            }
            copy_src();
            copy_lib();
            JOptionPane.showMessageDialog(null, "Done!");
        }
    }
    
    private void copy_lib()
    {
        String src_path = System.getProperty("user.dir")+"/Mlut/"+((String)mProjectType.getSelectedItem())+"\\lib";
        String dst=mRootDirectory.getText()+"\\"+((String)mProjectName.getSelectedItem())+"\\lib";
        
        if(!(new File(dst)).exists())
        {
            
                (new File(dst)).mkdir();
            
        }
        
        ArrayList<File> libs=new ArrayList<File>();
        File src_dir=new File(src_path);
        if (src_dir.exists()) 
        {
            File[] src_fs=src_dir.listFiles();
            for(File src_f : src_fs)
            {
                if(src_f.isDirectory()) continue;
                
                File df=new File(dst + "\\" + src_f.getName());
                try{
                  InputStream in = new FileInputStream(src_f);

                  OutputStream out = new FileOutputStream(df);

                  byte[] buf = new byte[1024];
                  int len;
                  while ((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                  }
                  in.close();
                  out.close();


                }catch(IOException ie)
                {
                    ie.printStackTrace();
                }
                
                if(src_f.getName().endsWith(".jar"))
                {
                    libs.add(df);
                }
            }
        }
        
        if(!libs.isEmpty())
        {
              String pp_path=mRootDirectory.getText()+"\\"+((String)mProjectName.getSelectedItem())+"\\nbproject\\project.properties";
              File pp_f=new File(pp_path);
              if(pp_f.exists()){
                  ArrayList<String> lines=new ArrayList<String>();
                  try {
                  //use buffering, reading one line at a time
                  //FileReader always assumes default encoding is OK!
                  BufferedReader input =  new BufferedReader(new FileReader(pp_f));
                  try {
                    String line = null; //not declared within while loop
                    /*
                    * readLine is a bit quirky :
                    * it returns the content of a line MINUS the newline.
                    * it returns null only for the END of the stream.
                    * it returns an empty String if two newlines appear in a row.
                    */
                    while (( line = input.readLine()) != null){
                      lines.add(line);
                    }
                  }
                  finally {
                    input.close();
                  }

                  BufferedWriter output=new BufferedWriter(new FileWriter(pp_f));

                  for(String line : lines)
                  {
                      
                      if(line.startsWith("javac.classpath="))
                      {
                          for(File lib : libs)
                          {
                            output.write("file.reference."+lib.getName()+"=lib/"+lib.getName()+"\n");
                          }
                      }
                      
                      boolean writable=true;
                      for(File lib : libs)
                      {
                          if(line.contains("file.reference."+lib.getName()+"=lib/"+lib.getName()))
                          {
                            writable=false;
                            break;
                          }
                          if(line.contains("${file.reference."+lib.getName()+"}"))
                          {
                              writable=false;
                              break;
                          }
                                  
                      }
                      
                      if(writable) output.write(line+"\n");
                      
                      if(line.startsWith("javac.classpath="))
                      {
                          for(File lib : libs)
                          {
                            output.write("\t${file.reference."+lib.getName()+"}:\\\n");
                          }
                      }
                  }
                  output.close();
                }
                catch (IOException ex){
                  ex.printStackTrace();
                }
                }
        }
    }
    
    private void copy_src()
    {
        String src_path = System.getProperty("user.dir")+"/Mlut/"+((String)mProjectType.getSelectedItem())+"\\src";
            
        String dst=mRootDirectory.getText()+"\\"+((String)mProjectName.getSelectedItem())+"\\src\\"+mPackagePath;
        File src_dir=new File(src_path);
        if (src_dir.exists()) {
            //JOptionPane.showMessageDialog(null, f.toString());
            File[] src_fs=src_dir.listFiles();
            for(File src_f : src_fs)
            {
                if(src_f.isDirectory()) continue;
                //JOptionPane.showMessageDialog(null, dst + "\\" + src_f.getName());

                File df=new File(dst + "\\" + src_f.getName());
                try{
                  InputStream in = new FileInputStream(src_f);

                  OutputStream out = new FileOutputStream(df);

                  byte[] buf = new byte[1024];
                  int len;
                  while ((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                  }
                  in.close();
                  out.close();


                }catch(IOException ie)
                {
                    ie.printStackTrace();
                }

                if(src_f.getName().endsWith(".java"))
                {
                  ArrayList<String> lines=new ArrayList<String>();
                  try {
                  //use buffering, reading one line at a time
                  //FileReader always assumes default encoding is OK!
                  BufferedReader input =  new BufferedReader(new FileReader(df));
                  try {
                    String line = null; //not declared within while loop
                    /*
                    * readLine is a bit quirky :
                    * it returns the content of a line MINUS the newline.
                    * it returns null only for the END of the stream.
                    * it returns an empty String if two newlines appear in a row.
                    */
                    while (( line = input.readLine()) != null){
                      lines.add(line);
                    }
                  }
                  finally {
                    input.close();
                  }

                  BufferedWriter output=new BufferedWriter(new FileWriter(df));

                  output.write("package "+mPackage.getText()+";\n");
                  output.write("//Mlut Generated Main.java ["+(String)mProjectType.getSelectedItem()+"] ["+Calendar.getInstance().toString()+"]\n");
                  for(String line : lines)
                  {
                      output.write(line+"\n");
                  }
                  output.close();
                }
                catch (IOException ex){
                  ex.printStackTrace();
                }
              }
            }
        }
    }
    
    public void setDialogDescriptor(DialogDescriptor desc) 
    { 
        this.desc = desc; 
        mRootDirectory.getDocument().addDocumentListener(this); 
    }
    
    private void doEnablement() { 
        if (mRootDirectory.getText().isEmpty()) 
        { 
            desc.setValid(false); 
        } 
        else 
        { 
            desc.setValid(true); 
        } 
    } 

    public void insertUpdate(DocumentEvent e) {
        doEnablement(); 
    }

    public void removeUpdate(DocumentEvent e) {
        doEnablement(); 
    }

    public void changedUpdate(DocumentEvent e) {
        doEnablement(); 
    }
}