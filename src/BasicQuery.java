import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BasicQuery {
    public BasicQuery(Scanner sysIn){
        scannerIn = sysIn;
    }

    public boolean FR2(){
        setLastName();
        while(getLastName().equalsIgnoreCase("")){
            System.out.println("-- Can not be empty String --");
            lastName = selectName();
        }

        if(getLastName().equalsIgnoreCase("q")){
            System.out.println("Creation Cancelled");
            return false;
        }

        setFirstName();
        while(getFirstName().equalsIgnoreCase("")){
            System.out.println("-- Can not be empty String --");
            firstName = selectName();
        }

        if(getFirstName().equalsIgnoreCase("q")){
            System.out.println("Creation Cancelled");
            return false;
        }

        setRoomCode();
        if(getRoomCode().equalsIgnoreCase("q")){
            System.out.println("Creation Cancelled");
            return false;
        }

        setCheckIn();
        while(getCheckIn().compareTo(EMPTY_DATE) == 0){
            System.out.println("-- Can not be empty String --");
            checkIn = selectDate();
        }
        if(getCheckIn().compareTo(QUIT_DATE) == 0){
            System.out.println("Creation Cancelled");
            return false;
        }

        setCheckOut();
        while(getCheckOut().compareTo(EMPTY_DATE) == 0){
            System.out.println("-- Can not be empty String --");
            checkOut = selectDate();
        }
        if(getCheckOut().compareTo(QUIT_DATE) == 0){
            System.out.println("Creation Cancelled");
            return false;
        }

        if(checkOut.compareTo(checkIn) < 0){
            System.out.println("Creation Cancelled. Conflicting Dates");
            return false;
        }

        setBedType();
        if(getBedType().equals("q")){
            System.out.println("Creation Cancelled");
            return false;
        }

        setChildren();
        if(getNumChildren() < 0){
            System.out.println("Creation Cancelled");
            return false;
        }

        setAdults();
        if(getNumAdults() < 0){
            System.out.println("Creation Cancelled");
            return false;
        }

        int totalOcc = numAdults + numChildren;
        int maxOcc = getMaxOcc();

        if(maxOcc == -1){
            System.out.println("Error in getting MaxOcc");
            return false;
        }

        if(totalOcc > maxOcc){
            System.out.println("Creation Cancelled. Too many occupants");
            return false;
        }

        uppercaseNames();


        return true;
    }
    public boolean FR3(){

        setReservationCODE();
        if(getReservationCode() <  0){
            System.out.println("Edit Cancelled");
            return false;
        }

        setLastName();
        if(getLastName().equalsIgnoreCase("q")){
            System.out.println("Edit Cancelled");
            return false;
        }

        setFirstName();
        if(getFirstName().equalsIgnoreCase("q")){
            System.out.println("Edit Cancelled");
            return false;
        }


        setCheckIn();
        if(getCheckIn().compareTo(QUIT_DATE) == 0){
            System.out.println("Edit Cancelled");
            return false;
        }

        setCheckOut();
        if(getCheckOut().compareTo(QUIT_DATE) == 0){
            System.out.println("Edit Cancelled");
            return false;
        }

        if(checkOut.compareTo(checkIn) < 0){
            System.out.println("Edit Cancelled. Conflicting Dates");
            return false;
        }

        setAdults();
        if(getNumAdults() < 0){
            System.out.println("Edit Cancelled");
            return false;
        }

        setChildren();
        if(getNumChildren() < 0){
            System.out.println("Edit Cancelled");
            return false;
        }

        int totalOcc = numAdults + numChildren;
        int maxOcc = getMaxOcc();

        if(maxOcc == -1){
            System.out.println("Error in getting MaxOcc");
            return false;
        }

        if(totalOcc > maxOcc){
            System.out.println("Edit Cancelled. Too many occupants");
            return false;
        }

        uppercaseNames();

        return true;
    }
    public boolean FR5(){
        System.out.println("Leave field empty for no change");
        setReservationCODE();

        setRoomCode();
        if(getRoomCode().equalsIgnoreCase("q")){
            System.out.println("Search Cancelled");
            return false;
        }

        setLastName();
        if(getLastName().equalsIgnoreCase("q")){
            System.out.println("Search Cancelled");
            return false;
        }

        setFirstName();
        if(getFirstName().equalsIgnoreCase("q")){
            System.out.println("Search Cancelled");
            return false;
        }

        setCheckInFR5();
        if(getCheckIn().compareTo(QUIT_DATE) == 0){
            System.out.println("Search Cancelled");
            return false;
        }

        setCheckOutFR5();
        if(getCheckOut().compareTo(QUIT_DATE) == 0){
            System.out.println("Search Cancelled");
            return false;
        }

        if(checkIn.compareTo(EMPTY_DATE) != 0 && checkOut.compareTo(EMPTY_DATE) != 0 && checkOut.compareTo(checkIn) < 0){
            System.out.println("Search Cancelled. Conflicting Dates");
            return false;
        }
        uppercaseNames();

        return true;
    }

    private void uppercaseNames(){
        lastName = lastName.toUpperCase(Locale.ROOT);
        firstName = firstName.toUpperCase(Locale.ROOT);
    }

    // SETTERS
    public void setReservationCODE(){
        // Allow to quit the creation of this
        System.out.print("Enter Reservation Code: ");
        reservationCode = selectReservationCode();
    }
    public void setReservationCode(int CODE){
        reservationCode = CODE;
    }
    public void setLastName(){
        // Allow to quit the creation of this
        System.out.print("Enter lastname: ");
        lastName = selectName();
    }
    public void setFirstName(){
        System.out.print("Enter firstname: ");
        firstName = selectName();
    }
    public void setRoomCode(){
        System.out.print("Enter a RoomCode (Leave empty or type 'Any' for any room): ");
        roomCode = selectRoomCode();
    }
    public void newRoomCode(String code){
        roomCode = code;
    }
    public void setBedType(){
        System.out.print("Enter Bed Type \n(King Queen Double)\n(Leave empty or type 'Any' for any bed): ");
        bedType = selectBedType();
    }
    public void setCheckIn(){
        // Allow to quit the creation of this
        System.out.print("Enter Check In (YYYY-MM-DD): ");
        checkIn = selectDate();
    }
    public void setCheckOut(){
        // Allow to quit the creation of this
        System.out.print("Enter Check Out (YYYY-MM-DD): ");
        checkOut = selectDate();
    }
    public void setCheckInFR5(){
        // Allow to quit the creation of this
        System.out.print("Enter Check In (YYYY-MM-DD): ");
        checkIn = selectDateFR5();
    }
    public void setCheckOutFR5(){
        // Allow to quit the creation of this
        System.out.print("Enter Check Out (YYYY-MM-DD): ");
        checkOut = selectDateFR5();
    }

    public void setChildren(){
        System.out.print("Enter how many children: ");
        numChildren = selectOcc(0,6);
    }
    public void setAdults(){
        System.out.print("Enter how many adults: ");
        numAdults = selectOcc(1,6);
    }



    // SELECTERS
    private String selectName(){
        return scannerIn.nextLine();
    }
    private String selectRoomCode(){
        return scannerIn.nextLine();
    }
    public int selectOcc(int lowL, int uppL){
        String ans;
        int num;
        while(true){
            ans = scannerIn.nextLine();

            if(ans.equals("q")){
                return -1;
            }
            else if(ans.equals("")){
                return 0;
            }

            try{
                num = Integer.parseInt(ans);

                if(num > lowL || num < uppL){
                    return num;
                }
                else{
                    System.out.println("Unreasonable integer");
                }

            }
            catch (Exception e){
                System.out.println("Invalid Input");
            }

        }
    }
    public String selectBedType(){
        return scannerIn.nextLine();
    }

    private Date selectDate() {
        String date = "";
        long millis = System.currentTimeMillis();
        Date currentDate = new Date(millis);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            date = scanner.nextLine();

            if (date.equalsIgnoreCase("q")) {
                return QUIT_DATE;
            }
            else if(date.equalsIgnoreCase("")){
                return EMPTY_DATE;
            }

            try {
                Date userDate = Date.valueOf(date);

                if (userDate.toString().equals(currentDate.toString()) || userDate.compareTo(currentDate) >= 0) {
                    return userDate;

                } else {
                    System.out.println("Invalid Date");
                }

            } catch (Exception e) {
                System.out.println("Invalid Input");
            }


        }
    }

    private Date selectDateFR5() {
        String date = "";
        Scanner scanner = new Scanner(System.in);

        while (true) {
            date = scanner.nextLine();

            if (date.equalsIgnoreCase("q")) {
                return QUIT_DATE;
            }
            else if(date.equalsIgnoreCase("")){
                return EMPTY_DATE;
            }

            try {
                return Date.valueOf(date);

            } catch (Exception e) {
                System.out.println("Invalid Input");
            }
        }
    }

    public int selectReservationCode(){
        String ans;
        int num = 0;
        while(true){
            ans = scannerIn.nextLine();

            if(ans.equals("q")){
                return -1;
            }
            else if(ans.equals("")){
                return 0;
            }

            try{
                num = Integer.parseInt(ans);
                break;
            }
            catch (Exception e){
                System.out.println("Invalid Input");
            }
        }

        return num;

    }
    public String getLastName() {
        return lastName;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getRoomCode(){
        return roomCode;
    }
    public Date getCheckIn() {
        return checkIn;
    }
    public Date getCheckOut() {
        return checkOut;
    }
    public int getNumChildren(){
        return numChildren;
    }
    public int getNumAdults(){
        return numAdults;
    }
    public String getBedType(){
        return bedType;
    }
    public int getReservationCode(){
        return reservationCode;
    }


    private int getMaxOcc(){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectMax = "SELECT max(maxOcc) AS mOcc FROM lab7_rooms";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectMax)) {

                try(ResultSet rs = pstmt.executeQuery()){

                    if(rs.next()){
                        return rs.getInt("mOcc");
                    }

                }
                catch (SQLException e){
                    e.getStackTrace();
                    conn.rollback();
                }
            }
            catch (SQLException e){
                e.getStackTrace();
                conn.rollback();
                return -1;
            }
        }
        catch (SQLException e){
            e.getStackTrace();
            return -1;
        }
        return -1;
    }

    private int reservationCode;
    private String lastName;
    private String firstName;
    private String roomCode; // Any no pref
    private String  bedType;    // Any no pref
    private Date checkIn;
    private Date checkOut;
    private int numChildren;
    private int numAdults;

    private Scanner scannerIn;
    public static Date QUIT_DATE = Date.valueOf("1990-01-01");
    public static Date EMPTY_DATE = Date.valueOf("1990-01-03");

}
