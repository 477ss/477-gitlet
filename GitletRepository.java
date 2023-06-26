package gitlet;

import java.io.File;
import java.util.*;

public class GitletRepository {
    /** folder path */
    static final File GITLET_FOLDER=Utils.join(".gitlet");
    static final File OBJECTS_FOLDER=Utils.join(GITLET_FOLDER,"objects");
    static final File COMMIT_FOLDER=Utils.join(OBJECTS_FOLDER,"commit");
    /** file path */
    static final File REFS_FOLDER=Utils.join(GITLET_FOLDER,"refs");
    static final File REFS_HEADS_FOLDER=Utils.join(REFS_FOLDER,"heads");
    static final File HEADS_MASTER_FILE=Utils.join(REFS_HEADS_FOLDER,"master");
    static final File STAGE_FILE=Utils.join(Utils.join(GITLET_FOLDER,"stage"),"stage");
    static final File HEAD_FILE=Utils.join(Utils.join(GITLET_FOLDER,"HEAD"),"head");
    static final File BLOB_FOLDER=Utils.join(OBJECTS_FOLDER,"blob");
    /** remote */
    static final File REMOTE_REPO_FOLDER=Utils.join(GITLET_FOLDER,"remote_repo");
    static final File REFS_REMOTE_FOLDER=Utils.join(REFS_FOLDER,"remote");
    /** index */
    static final File INDEX_FILE=Utils.join(OBJECTS_FOLDER,"index");

    /** gitlet init*/
    public static void init(){
        final File BLOB_FOLDER=Utils.join(OBJECTS_FOLDER,"blob");
        final File STAGE_FOLDER=Utils.join(GITLET_FOLDER,"stage");
        final File HEAD_FOLDER=Utils.join(GITLET_FOLDER,"HEAD");

        try {
            if(GITLET_FOLDER.exists()){
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }
            GITLET_FOLDER.mkdir();
            OBJECTS_FOLDER.mkdir();
            BLOB_FOLDER.mkdir();
            COMMIT_FOLDER.mkdir();
            STAGE_FOLDER.mkdir();
            HEAD_FOLDER.mkdir();
            REFS_FOLDER.mkdir();
            REFS_HEADS_FOLDER.mkdir();
            REMOTE_REPO_FOLDER.mkdir();
            REFS_REMOTE_FOLDER.mkdir();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Index index=new Index();
        index.save();
        Commit commit_init=new Commit();
        commit_init.save();
        Head head=new Head();
        head.save();
        Stage stage=new Stage();
        stage.save();
        Refs master=new Refs(commit_init.id, "master");
        master.save();

    }

    /** commit files in stage */
    public static Commit commit(String message){
        if(message.length()==0){
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        HashMap<String,String> stage_blob_list=stage.get_stage_blob();
        LinkedList<String> rm_blob_list=stage.get_rm_blob();
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_branch=head.get_cur_branch();
        Commit parent=cur_branch.get_cur_commit();

        if(stage_blob_list.size()==0 && rm_blob_list.size()==0){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        HashMap<String, String> commit_blob_list = new HashMap<>(parent.blob_list);
        for(HashMap.Entry<String, String> entry : stage_blob_list.entrySet()){
            if(!commit_blob_list.containsKey(entry.getKey()) || !commit_blob_list.containsValue(entry.getValue())){
                commit_blob_list.put(entry.getKey(),entry.getValue());
            }
        }

        Iterator<HashMap.Entry<String, String>> iterator=commit_blob_list.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            if(rm_blob_list.contains(entry.getValue())){
                iterator.remove();
            }
        }
        Commit commit=new Commit(message,parent.id,commit_blob_list);
        commit.save();
        cur_branch.update_cur_commit(commit);
        cur_branch.save();
        stage.clear();
        stage.save();

        return commit;
    }

    /** add file to stage, only one file at a time*/
    public static void add(String file_path){
        File f=Utils.join(file_path);
        if(!f.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob b=new Blob(f.getName(),Utils.readContents(f),file_path);
        b.save();
        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        stage.add(b);
        stage.save();
    }

    /** rm file, which is in stage or current commit */
    public static void rm(String file_path){
        File f=Utils.join(file_path);
        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit cur_commit=head.get_cur_commit();

        if(stage.blob_list.containsKey(f.getPath()) || cur_commit.blob_list.containsKey(f.getPath())){
            stage.blob_list.remove(f.getPath());
            if(cur_commit.blob_list.containsKey(f.getPath())){
                String id=cur_commit.blob_list.get(f.getPath());
                if(id!=null){
                    stage.rm_blob_list.add(id);
                }
                f.delete();
            }
        }else{
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        stage.save();
    }

    public static Commit get_commit(String id){
        Index index=Utils.readObject(INDEX_FILE, Index.class);
        return index.get_commit(id);
    }

    public static Blob get_blob(String id){
        Index index=Utils.readObject(INDEX_FILE, Index.class);
        return index.get_blob(id);
    }

    public static Refs get_branch(String branch_name){
        File f=Utils.join(REFS_HEADS_FOLDER,branch_name);
        if(!f.exists()){
            f=Utils.join(REFS_REMOTE_FOLDER,branch_name);
            if(!f.exists()){
                return null;
            }
        }
        return Utils.readObject(f,Refs.class);
    }

    private static File get_branch_file(String branch_name){
        File f=Utils.join(REFS_HEADS_FOLDER,branch_name);
        if(!f.exists()){
            f=Utils.join(REFS_REMOTE_FOLDER,branch_name);
            if(!f.exists()){
                return null;
            }
        }
        return f;
    }

    private static Refs get_remote_branch(String repo_name,String branch_name){
        File f=Utils.join(REFS_REMOTE_FOLDER,repo_name,branch_name);
        if(!f.exists()){
            return null;
        }
        return Utils.readObject(f,Refs.class);
    }

    /** print message of current branch */
    public static void log(){
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit commit=head.get_cur_commit();
        while(commit.parent.size()!=0){
            System.out.print(commit);
            commit=get_commit(commit.parent.get(0));
        }
        System.out.print(commit);
    }

    /** print message of all commit */
    public static void global_log(){
        File[] file_list=COMMIT_FOLDER.listFiles();
        for(File f:file_list){
            System.out.print(Utils.readObject(f,Commit.class));
        }
    }

    /**print commit id which has the given message*/
    public static void find(String message){
        File[] file_list=COMMIT_FOLDER.listFiles();
        int flag=0;
        for(File f:file_list){
            Commit commit=Utils.readObject(f,Commit.class);
            if(commit.message.equals(message)){
                System.out.println(commit.id);
                flag=1;
            }
        }
        if(flag==0){
            System.out.println("Found no commit with that message.");
        }
    }

    /**print status of git*/
    public static void status(){
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_branch=head.get_cur_branch();

        List<String> local_branch_list=Utils.plainFilenamesIn(REFS_HEADS_FOLDER);
        System.out.println("=== Branches ===");
        for(String b:local_branch_list){
            if(b.equals(cur_branch.ref_name)){
                System.out.printf("*%s\n",b);
            }else{
                System.out.println(b);
            }
        }
        System.out.println("\n=== Staged Files ===");
        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        stage.print();
        System.out.println("\n=== Removed Files ===");
        stage.rm_print();
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    /** checkout file from current branch */
    public static void checkout(String[] args){
        switch (args.length) {
            case 3 :
                if(!args[1].equals("--")){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                checkout_file(args[2]);
                break;
            case 4:
                if(!args[2].equals("--")){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                checkout_file(args[1], args[3]);
                break;
            case 2 :
                checkout_branch(args[1]);
        }
    }

    /** checkout file from current commit */
    private static void checkout_file(String file_name){
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit commit=head.get_cur_commit();
        File f=Utils.join(file_name);
        String id=commit.blob_list.get(f.getPath());

        if(id==null){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob cb=get_blob(id);
        Utils.writeContents(f,cb.content);
    }

    /** checkout file from given commit */
    private static void checkout_file(String commit_id, String file_name){
        Commit commit=get_commit(commit_id);
        if(commit==null){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File f=Utils.join(file_name);
        String id=commit.blob_list.get(f.getPath());

        if(id==null){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob cb=get_blob(id);
        Utils.writeContents(f,cb.content);
    }

    public static String count_hash(File f){
        return Utils.sha1(f.getPath(),Utils.readContents(f));
    }

    private static void checkout_branch(String branch_name){
        File branch_file=get_branch_file(branch_name);
        if(branch_file==null){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Refs branch=Utils.readObject(branch_file,Refs.class);

        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_ref=head.get_cur_branch();

        if(cur_ref.equals(branch)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit checkout_commit=branch.get_cur_commit();
        checkout_commit(checkout_commit);

        head.cur_ref_file=branch_file;
        head.save();
    }

    /** create new local_ref */
    public static void branch(String branch_name){
        File f=Utils.join(REFS_HEADS_FOLDER,branch_name);
        if(f.exists()){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit cur_commit=head.get_cur_commit();
        Refs ref=new Refs(cur_commit.id, branch_name);
        ref.save();
    }

    /** delete local_ref */
    public static void rm_branch(String branch_name){
        File f=Utils.join(REFS_HEADS_FOLDER,branch_name);
        if(!f.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Refs ref=Utils.readObject(f, Refs.class);
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_branch=head.get_cur_branch();
        if(cur_branch.equals(ref)){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        f.delete();
    }

    /** checkout given commit */
    public static void reset(String commit_id){
        Commit checkout_commit=get_commit(commit_id);
        if(checkout_commit==null){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        checkout_commit(checkout_commit);
        Head head=Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_ref=head.get_cur_branch();
        cur_ref.update_cur_commit(checkout_commit);
        cur_ref.save();
    }

    @Deprecated
    public static void reset(Commit ck_commit, Commit cur_commit, String des_path){
        checkout_commit(ck_commit,cur_commit,des_path);
        File head_file=Utils.join(des_path,".gitlet","HEAD","head");
        Head head=Utils.readObject(head_file,Head.class);
        Refs des_head_ref=Utils.readObject(head.cur_ref_file,Refs.class);
        des_head_ref.update_cur_commit(ck_commit);
        Utils.writeObject(head.cur_ref_file,des_head_ref);
    }

    /** checkout given commit to des_path*/
    private static void checkout_commit(Commit checkout_commit){
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit cur_commit=head.get_cur_commit();

        //若(工作区)有在签出提交中，且不在当前提交中的文件，告警退出(存在覆盖危险)
        for(String path:checkout_commit.blob_list.keySet()){
            File f=Utils.join(path); //
            //文件存在，检查文件是否tracked
            if(f.exists()){
                String id=count_hash(f);
                //比较当前commit中的版本和工作目录中的版本
                String blob_id=cur_commit.blob_list.get(f.getPath());
                if(!id.equals(blob_id)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        //签出提交：覆盖已经commit的文件和创建不存在的文件
        for(Map.Entry<String,String> entry: checkout_commit.blob_list.entrySet()){
            File f=Utils.join(entry.getKey()); //
            if(f.exists()){
                //检查签出文件和工作目录中版本是否相同
                String id=count_hash(f);
                if(id.equals(entry.getValue())){
                    continue;
                }
            }
            Blob b=get_blob(entry.getValue());
            Utils.writeContents(f,b.content);
        }

        //删除(工作区中)在当前commit中且不在签出提交中的文件
        for(Map.Entry<String,String> entry: cur_commit.blob_list.entrySet()){
            File f=Utils.join(entry.getKey()); //
            String f_id=count_hash(f);
            if(f_id.equals(entry.getValue())){
                if(!checkout_commit.blob_list.containsKey(entry.getKey())){
                    f.delete();
                }
            }
        }

        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        stage.clear();
        stage.save();
    }

    /** des_path: 目标工作目录(仓库所在地址) */
    @Deprecated
    public static void checkout_commit(Commit checkout_commit, Commit cur_commit, String des_path){
        //若(工作区)有在签出提交中，且不在当前提交中的文件，告警退出(存在覆盖危险)
        for(String path:checkout_commit.blob_list.keySet()){
            File f=Utils.join(des_path,path); //
            //文件存在，检查文件是否tracked
            if(f.exists()){
                String id=Utils.sha1(path,Utils.readContents(f));
                //比较当前commit中的版本和工作目录中的版本
                String blob_id=cur_commit.blob_list.get(f.getPath());
                String ck_id=checkout_commit.blob_list.get(path);
                if(!id.equals(blob_id) && !id.equals(ck_id)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        //签出提交：覆盖已经commit的文件和创建不存在的文件
        for(Map.Entry<String,String> entry: checkout_commit.blob_list.entrySet()){
            File f=Utils.join(des_path, entry.getKey()); //
            if(f.exists()){
                //检查签出文件和工作目录中版本是否相同
                String id=count_hash(f);
                if(id.equals(entry.getValue())){
                    continue;
                }
            }
            Blob b=get_blob(entry.getValue());
            Utils.writeContents(f,b.content);
        }

        //删除(工作区中)在当前commit中且不在签出提交中的文件
        for(Map.Entry<String,String> entry: cur_commit.blob_list.entrySet()){
            File f=Utils.join(des_path,entry.getKey()); //
            String f_id=count_hash(f);
            if(f_id.equals(entry.getValue())){
                if(!checkout_commit.blob_list.containsKey(entry.getKey())){
                    f.delete();
                }
            }
        }

        //Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        File des_stage=Utils.join(des_path,".gitlet","stage","stage");
        Stage stage=Utils.readObject(des_stage,Stage.class);
        stage.clear();
        stage.save();
    }

    private static void sp_helper(Commit c, HashMap<String,Integer> visit, int num){
        visit.put(c.id,num);
        for(String p: c.parent){
            sp_helper(get_commit(p),visit,num+1);
        }
    }

    private static int min(int a,int b){
        return a<b?a:b;
    }

    private static int sp_helper(Commit c, HashMap<String,Integer> visit){
        if(visit.containsKey(c.id)){
            return visit.get(c.id);
        }

        if(c.parent.size()==0){
            return visit.size();
        }else if(c.parent.size()==1){
            return sp_helper(get_commit(c.parent.get(0)),visit);
        }else{
            return min(sp_helper(get_commit(c.parent.get(0)),visit),sp_helper(get_commit(c.parent.get(1)),visit));
        }

    }

    /** return the split point of the two commit */
    private static Commit find_split_point(Commit c1, Commit c2){
        HashMap<String,Integer> v=new HashMap<>();
        sp_helper(c1,v,0);
        int ret=sp_helper(c2,v);
        for(Map.Entry<String,Integer> entry:v.entrySet()){
            if(entry.getValue()==ret){
                return get_commit(entry.getKey());
            }
        }
        return null;
    }

    /** merge local ref*/
    public static void merge(String branch_name){
        Refs branch= get_branch(branch_name);
        int ret=merge(branch);
        if(ret!=-1){
            Head head =Utils.readObject(HEAD_FILE,Head.class);
            Refs cur_ref=head.get_cur_branch();
            Commit new_commit=commit(String.format("Merged %s into %s.",branch.ref_name,cur_ref.ref_name));
            Commit checkout_commit=branch.get_cur_commit();
            new_commit.parent.add(checkout_commit.id);
            new_commit.save();
        }
    }

    private static void remote_merge(String repo_name, String branch_name){
        Refs branch= get_remote_branch(repo_name,branch_name);
        int ret=merge(branch);
        if(ret!=-1){
            Head head =Utils.readObject(HEAD_FILE,Head.class);
            Refs cur_ref=head.get_cur_branch();
            Commit new_commit=commit(String.format("Merged %s/%s into %s.",repo_name,branch.ref_name,cur_ref.ref_name));
            Commit checkout_commit=branch.get_cur_commit();
            new_commit.parent.add(checkout_commit.id);
            new_commit.save();
        }
    }

    public static int merge(Refs branch){
        Stage stage=Utils.readObject(STAGE_FILE,Stage.class);
        if(stage.blob_list.size()!=0 || stage.rm_blob_list.size()!=0){
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if(branch==null){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Refs cur_ref=head.get_cur_branch();
        Commit cur_commit=head.get_cur_commit();
        Commit checkout_commit=branch.get_cur_commit();
        if(cur_ref.equals(branch)){
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        //若(工作区)有在签出提交中，且不在当前提交中的文件，告警退出(存在覆盖危险)
        for(String path:checkout_commit.blob_list.keySet()){
            File f=Utils.join(path);
            //文件存在，检查文件是否tracked
            if(f.exists()){
                String id=count_hash(f);
                //比较当前commit中的版本和工作目录中的版本
                String blob_id=cur_commit.blob_list.get(f.getPath());
                if(!id.equals(blob_id)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        //寻找分裂点
        Commit sp_commit=find_split_point(checkout_commit,cur_commit);
        //当前分支是分裂点: 检出给定分支
        if(sp_commit.id.equals(cur_commit.id)){
            checkout_commit(checkout_commit);
            System.out.println("Current branch fast-forwarded.");
            return -1;
        }
        //给定分支是分裂点：合并结束
        if(sp_commit.id.equals(checkout_commit.id)){
            System.out.println("Given branch is an ancestor of the current branch.");
            return -1;
        }

        for(Map.Entry<String,String> entry: cur_commit.blob_list.entrySet()){
            String ck_id= checkout_commit.blob_list.get(entry.getKey());
            String sp_id=sp_commit.blob_list.get(entry.getKey());
            //给定分支无修改：保持原状
            if(ck_id!=null && ck_id.equals(sp_id)){
                continue;
            }
            //存在于分离点
            if(sp_id!=null){
                //当前分支未修改
                if(sp_id.equals(entry.getValue())){
                    //给定分支中已删除: rm
                    if(ck_id==null){
                        stage.rm_blob_list.add(entry.getValue());
                        File f=Utils.join(entry.getKey());
                        if(f.exists()){
                            f.delete();
                        }
                    }//给定分支已修改：检出，暂存
                    else{
                        //checkout_blob(ck_id);
                        Blob b=get_blob(ck_id);
                        File f=Utils.join(b.path);
                        Utils.writeContents(f, b.content);
                        stage.add(b);
                        stage.save();
                    }
                }//当前分支已修改
                else{
                    //合并冲突（两个分支做了不同修改）
                    //file_merge(ck_id, entry.getValue());
                    String s=conflictFileContents(entry.getValue(),ck_id);
                    File f=Utils.join(entry.getKey());
                    Utils.writeContents(f,s);
                    add(entry.getKey());
                    System.out.println("Encountered a merge conflict.");
                }
            }//分离点不存在，当前分支和给定分支版本不同：合并冲突
            else if(ck_id!=null && !ck_id.equals(entry.getValue())){
                //合并冲突（两个分支分别新增同路径文件且内容不同）
                //file_merge(ck_id, entry.getValue());
                String s=conflictFileContents(entry.getValue(),ck_id);
                File f=Utils.join(entry.getKey());
                Utils.writeContents(f,s);
                add(entry.getKey());
                System.out.println("Encountered a merge conflict.");
            }
        }

        for(Map.Entry<String,String> entry: checkout_commit.blob_list.entrySet()){
            String cur_id= cur_commit.blob_list.get(entry.getKey());
            String sp_id=sp_commit.blob_list.get(entry.getKey());
            //分离点和当前分支中均不存在，给定分支新增的文件：签出、暂存
            if(sp_id==null && cur_id==null){
                Blob b=get_blob(entry.getValue());
                File f=Utils.join(b.path);
                Utils.writeContents(f, b.content);
                stage.add(b);
                stage.save();
            }
        }
        /*
        Commit new_commit=commit(String.format("Merged %s into %s.",branch.ref_name,cur_ref.ref_name));
        new_commit.parent.add(checkout_commit.id);
        new_commit.save();
        */
        return 0;
    }

    private static String conflictFileContents(String currentBlobId, String mergedBlobId) {
        String currentContents;
        String mergedContents;
        if (currentBlobId == null) {
            currentContents = "";
        } else {
            Blob b1=get_blob(currentBlobId);
            currentContents = new String(b1.content);
        }
        if (mergedBlobId == null) {
            mergedContents = "";
        } else {
            Blob b2=get_blob(mergedBlobId);
            mergedContents = new String(b2.content);
        }
        return "<<<<<<< HEAD\n" + currentContents + "=======\n" + mergedContents + ">>>>>>>\n";
    }

    /** add remote repo */
    public static void add_remote(String name, String path){
        File f=Utils.join(REMOTE_REPO_FOLDER,name);
        if(f.exists()){
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        Remote remote=new Remote(name,Utils.join(path).getParent());
        remote.save();
        f=Utils.join(REFS_REMOTE_FOLDER,name);
        f.mkdir();
    }

    /** delete remote repo */
    public static void rm_remote(String name){
        File f=Utils.join(REMOTE_REPO_FOLDER,name);
        if(!f.delete()){
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        f=Utils.join(REFS_REMOTE_FOLDER,name);
        File[] files=f.listFiles();
        for(File file:files){
            file.delete();
        }
        f.delete();
    }

    private static File get_remote_root(String name){
        File remote_repo_file=Utils.join(REMOTE_REPO_FOLDER,name);
        Remote remote_repo=Utils.readObject(remote_repo_file,Remote.class);

        File remote_root=Utils.join(remote_repo.path,".gitlet");
        if(!remote_root.exists()){
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        return remote_root;
    }

    public static void push(String name, String branch_name){
        File repo_file=get_remote_repo(name);
        File remote_root=get_remote_root(name);
        File remote_ref_path=Utils.join(remote_root,"refs","heads",branch_name);
        File remote_commit_folder = Utils.join(remote_root, "objects", "commit");
        File remote_blob_folder=Utils.join(remote_root,"objects","blob");
        File remote_index_file=Utils.join(remote_root,"objects","index");
        Index remote_index=Utils.readObject(remote_index_file, Index.class);

        Commit remote_commit=null;
        Refs remote_ref=null;

        Head local_head=Utils.readObject(HEAD_FILE,Head.class);
        Commit cur_commit = local_head.get_cur_commit();

        if(remote_ref_path.exists()){
            remote_ref = Utils.readObject(remote_ref_path, Refs.class);
            File remote_commit_path = Utils.join(remote_commit_folder, remote_ref.cur_commit_id);
            remote_commit = Utils.readObject(remote_commit_path, Commit.class);
        }

        //检查远程分支头部是否在当前分支历史提交中
        LinkedList<Commit> visit_queue=new LinkedList<>();
        LinkedList<Commit> add_queue=new LinkedList<>();
        visit_queue.push(cur_commit);
        Commit commit=null;
        while(!visit_queue.isEmpty()){
            commit=visit_queue.removeFirst();
            if(remote_commit!=null && commit.id.equals(remote_commit.id)){
                break;
            }
            add_queue.push(commit);
            for(String p: commit.parent){
                visit_queue.addFirst(get_commit(p));
            }
        }
        //远程分支不在当前历史分支中
        if(remote_commit!=null && !commit.id.equals(remote_commit.id)){
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }else{
            for(Commit c:add_queue){
                //add commit
                File commit_file=Utils.join(remote_commit_folder,c.id);
                Utils.writeObject(commit_file,c);
                remote_index.put_commit(c.id,Utils.join(COMMIT_FOLDER,c.id));
                //add blob
                for(String blob_id:c.blob_list.values()){
                    if(!remote_index.has_blob(blob_id)){
                        File blob_file=Utils.join(remote_blob_folder,blob_id);
                        Utils.writeObject(blob_file,get_remote_blob(repo_file,remote_index,blob_id));
                        remote_index.put_blob(blob_id,Utils.join(BLOB_FOLDER,blob_id));
                    }
                }
            }
        }
        Utils.writeObject(remote_index_file,remote_index);
        if(remote_ref==null){
            remote_ref=new Refs(cur_commit.id,branch_name);
            Utils.writeObject(remote_ref_path,remote_ref);
        }else{
            remote_ref.cur_commit_id=cur_commit.id;
            Utils.writeObject(remote_ref_path,remote_ref);
        }

        //reset
        File remote_head_file=Utils.join(remote_root,"HEAD","head");
        Head remote_head=Utils.readObject(remote_head_file, Head.class);
        Refs remote_cur_ref=Utils.readObject(remote_head.cur_ref_file, Refs.class);
        Commit remote_cur_commit=get_remote_commit(repo_file,remote_index,remote_cur_ref.cur_commit_id);
        reset(cur_commit,remote_cur_commit,repo_file.getPath());
    }

    /**return: 远程仓库地址*/
    private static File get_remote_repo(String name){
        File remote_repo_file=Utils.join(REMOTE_REPO_FOLDER,name);
        if(!remote_repo_file.exists()){
            return null;
        }
        Remote remote_repo=Utils.readObject(remote_repo_file,Remote.class);
        return Utils.join(remote_repo.path);
    }

    private static Commit get_remote_commit(File repo, Index index, String id){
        File commit_file=index.commit_index.get(String.format("%.6s",id));
        if (commit_file==null){
            return null;
        }
        commit_file=Utils.join(repo,commit_file.getPath());
        return Utils.readObject(commit_file, Commit.class);
    }

    private static Blob get_remote_blob(File repo, Index index, String id){
        File blob_file=index.blob_index.get(String.format("%.6s",id));
        if (blob_file==null){
            return null;
        }
        blob_file=Utils.join(repo,blob_file.getPath());
        return Utils.readObject(blob_file, Blob.class);
    }

    public static void fetch(String name, String branch_name){
        File repo_file=get_remote_repo(name);
        File remote_root=get_remote_root(name);
        File remote_ref_path=Utils.join(remote_root,"refs","heads",branch_name);
        File remote_index_file=Utils.join(remote_root,"objects","index");
        Index remote_index=Utils.readObject(remote_index_file, Index.class);

        if(!remote_ref_path.exists()){
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        Refs remote_ref=Utils.readObject(remote_ref_path, Refs.class);
        Commit remote_commit=get_remote_commit(repo_file,remote_index,remote_ref.cur_commit_id);

        Head local_head=Utils.readObject(HEAD_FILE,Head.class);
        Commit cur_commit=local_head.get_cur_commit();
        Index local_index=Utils.readObject(INDEX_FILE, Index.class);

        if(cur_commit.id.equals(remote_commit.id)){
            File f=Utils.join(REFS_REMOTE_FOLDER,name,branch_name);
            Utils.writeObject(f,remote_ref);
            System.exit(0);
        }

        Commit commit;
        LinkedList<Commit> visit_queue=new LinkedList<>();
        visit_queue.addFirst(remote_commit);

        while(!visit_queue.isEmpty()){
            commit=visit_queue.removeFirst();

            if(local_index.has_commit(commit.id)){
                continue;
            }

            File commit_file=Utils.join(COMMIT_FOLDER,commit.id);
            Utils.writeObject(commit_file,commit);
            local_index.put_commit(commit.id,commit_file);

            for(String blob_id:commit.blob_list.values()){
                if(!local_index.has_blob(blob_id)){
                    File blob_file=Utils.join(BLOB_FOLDER,blob_id);
                    Utils.writeObject(blob_file,get_remote_blob(repo_file,remote_index,blob_id));
                    local_index.put_blob(blob_id,blob_file);
                }
            }

            for(String p: commit.parent){
                visit_queue.addFirst(get_remote_commit(repo_file,remote_index,p));
            }
        }

        Utils.writeObject(remote_index_file,remote_index);
        Utils.writeObject(INDEX_FILE,local_index);

        File f=Utils.join(REFS_REMOTE_FOLDER,name,branch_name);
        Utils.writeObject(f,remote_ref);


    }

    public static void pull(String name, String branch_name){
        fetch(name,branch_name);
        remote_merge(name,branch_name);
    }
}
