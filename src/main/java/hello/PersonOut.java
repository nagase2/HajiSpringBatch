package hello;

public class PersonOut {
    private String lastName;
    private String firstName;
    private String lastName2;
    private String firstName2;
    
    
    public PersonOut(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
  }
    
    public String getLastName2() {
      return lastName2;
    }

    public void setLastName2(String lastName2) {
      this.lastName2 = lastName2;
    }

    public String getFirstName2() {
      return firstName2;
    }

    public void setFirstName2(String firstName2) {
      this.firstName2 = firstName2;
    }

    public PersonOut() {

    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName;
    }

}
