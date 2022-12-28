import java.util.ArrayList;

public class Node {
    private Token token;//表示递归树的每个节点都对应一个token
    private ArrayList<Node> childList;//表示它的子节点序列 从左往右

    public Node(Token token) {
        this.token = token;
        this.childList = new ArrayList<>();
    }

    public void addChild(Node child) {
        this.childList.add(child);
    }

    public void print() {
        String newType = "";
        if (SyntaxParser.reverseList.contains(this.token.getType())) {
            newType = this.token.getType().substring(0, this.token.getType().length() - 2) + ">";
        } else {
            newType = this.token.getType();
        }
        if (this.token.getWord().length() == 0) {
            System.out.println(newType);
        } else {
            System.out.println(newType + " " + this.token.getWord());
        }
    }

    public ArrayList<Node> getChildList() {
        return this.childList;
    }

    public Token getToken() {
        return this.token;
    }


}
