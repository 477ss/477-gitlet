package gitlet;

import java.io.File;
import java.io.Serializable;

/**远程仓库*/
public class Remote implements Serializable {
    static final File REMOTE_REPO_FOLDER=GitletRepository.REMOTE_REPO_FOLDER;
    /** short name */
    String name;
    String path;

    public Remote(String name, String path){
        this.name=name;
        this.path=path;
    }

    public void save(){
        File f=Utils.join(REMOTE_REPO_FOLDER,name);
        Utils.writeObject(f,this);
    }


}
