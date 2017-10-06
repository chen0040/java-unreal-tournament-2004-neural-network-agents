package com.codezone.mlutmod;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;

class MlutLauncher  extends CallableSystemAction
{    
    public MlutLauncher()
    {
        
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }    
    
    //Added
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected String iconResource() {
        //Replace org/nvarun/tat with your path/to/icon
        return "com/codezone/mlutmod/mlut_icon.png";
    }
    //see attachments to download icon24_CreateAlwaysEnabledActionFromScratch.png
    
    private void launchWelcome()
    {
        String msg = "Welcome to Mlut!";
        NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);  
    }
    
    private void launchDialog()
    {        
        MlutModStarter frm = new MlutModStarter(); 

        DialogDescriptor desc = new DialogDescriptor(frm, 
        "Hello", true, DialogDescriptor.OK_CANCEL_OPTION, 
        DialogDescriptor.OK_OPTION, null); 

        desc.setValid(false); 
        frm.setDialogDescriptor(desc); 
        DialogDisplayer.getDefault().notify(desc); // displays the dialog 
    }
    
    @Override
    public void performAction() {
         //throw new UnsupportedOperationException("Not supported yet.");
        launchWelcome();
        launchDialog();
    }

    @Override
    public String getName() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return NbBundle.getMessage(MlutLauncher.class, "CTL_MlutLauncher");
    }

    @Override
    public HelpCtx getHelpCtx() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return HelpCtx.DEFAULT_HELP;
    }
}