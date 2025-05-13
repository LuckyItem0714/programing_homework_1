public class Account{
    String owner;
    int balance;
    public Account(String owner, int balance){
        this.owner = owner;
        this.balance = balance;
    }
    public void transfer(Account dest, int amount){
        dest.balance += amount;
        balance -= amount;
    }
}
