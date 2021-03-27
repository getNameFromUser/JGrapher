/**
  *
  * Beschreibung
  *
  * @version 1.0 vom 07.12.2017
  */
public class PriorityQueue<ContentType> {
  private class PriorityNode<ContentType> {
    private ContentType content;
    private PriorityNode<ContentType> nextNode;
    private double priority;

    public PriorityNode(ContentType pContent, double pPriority) {
      content = pContent;
      nextNode = null;

      if (pPriority >= 0) {
        this.priority = pPriority;
      } // end of if
      this.priority = pPriority;
        
    }

    public void setNextNode(PriorityNode<ContentType> pNext) {
      nextNode = pNext;
    }

    public PriorityNode<ContentType> getNextNode() {
      return nextNode;
    }

    public double getPriority() {
      return priority;
    }

    public ContentType getContent() {
      return content;
    }
  }

  // Anfang Attribute
  private PriorityNode<ContentType> head;
  private PriorityNode<ContentType> tail;

  // Ende Attribute
  public PriorityQueue() {
    head = null;
    tail = null;
  }

  // Anfang Methoden
  public boolean isEmpty() {
    return head == null;
  }

  public void dequeue() {
    if (!this.isEmpty()) {
      head = head.getNextNode();
    }
  }

  public double frontPriority() {
    if (this.isEmpty()) {
      return 0;
    } else {
      return head.getPriority();
    }
  }

  public ContentType front() {
    if (this.isEmpty()) {
      return null;
    } else {
      return head.getContent();
    }
  }

  public void enqueue(ContentType pContent, double pPriority) {
    if (pContent != null) { //Neues Objekt ist auch eins.

      PriorityNode<ContentType> newOne = new PriorityNode<ContentType>(pContent,
                                                                       pPriority);

      if (this.isEmpty()) { //Wenn die Schlange leer ist, ... 
                            //... wird der neue Knoten der erste und der letzte.
        head = newOne;
        tail = newOne;
      } else if (tail.getPriority() >= pPriority) { //Wenn neue Priorität groesser letzte ist,
                                                    //an Ende anfügen
        tail.setNextNode(newOne);
        tail = newOne;
        tail.setNextNode(null);
      } else if (head.getPriority() > newOne.getPriority()) { // Wenn es das mit der kleinstens Priorität ist,
        newOne.setNextNode(head); // vorne einfügen.
        head = newOne;
      } else {
        //Wenn es einsortiert werden muss...
        PriorityNode<ContentType> temp = head;

        while ((temp.getNextNode()).getPriority() <= newOne.getPriority()) {
          temp = temp.getNextNode();
        }

        newOne.setNextNode(temp.getNextNode());
        temp.setNextNode(newOne);
      }
    }
  }
  public String toString() {
    PriorityQueue<ContentType> tmp = new PriorityQueue<ContentType>();
    String res = "";

    //prioEdges = new PriorityQueue<Edge>();
    while (!this.isEmpty()) {
      ContentType c = this.front();
      double p = this.frontPriority();
      System.out.println(p);
      res =res + (p + ",");
      tmp.enqueue(c, p);
      this.dequeue();
    } // end of while

    while (!tmp.isEmpty()) {
      ContentType c = this.front();
      double p = this.frontPriority();
      
      this.enqueue(c, p);
      tmp.dequeue();
    } // end of while
    return (res);
  }

  // Ende Methoden
} // end of PriorityQueue
