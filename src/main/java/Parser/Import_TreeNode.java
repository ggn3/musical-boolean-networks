package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

public class Import_TreeNode extends MutableTreeNodeImpl{
    String fileName;

    public Import_TreeNode(String fileName){
        this.fileName = fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}

