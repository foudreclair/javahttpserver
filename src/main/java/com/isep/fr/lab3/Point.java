package com.isep.fr.lab3;
//programme infini provoque un crash du pc
public class Point extends Thread
{
  protected int x;
  protected int y;
  
  public void moveTo(int x, int y)
  {
    
    this.x = x;
    for (int i=0 ; i<99999 ; i++)
      ; //donothing
    this.y = y;
  }
  
  public String toString()
  {
    return (x==y)?"(" + x + "," + y + ")" : "\n"+"(" + x + "," + y + ")" + "\n";
  }

  public static void main(String[] argv)
  {
   Point p = new Point();
    Thread thread = new Thread(p);
    thread.start();
    while(true)
    {
      p.moveTo(1,1);
      System.out.print(p);
    }
  }

  public void run()
  {
    while(true)
    {
      yield();
      this.moveTo(0,0);
      System.out.print(this);
    } 
  }
}