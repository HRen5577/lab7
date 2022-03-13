import java.sql.*;
import java.sql.Date;
import java.util.*;

public class InnReservations {
    public static void main(String[] args) {
        try {
            new InnReservations();
        }
        catch (Exception e){
            System.out.println("Inn reservation not created");
        }
    }
    public InnReservations() throws Exception {

        //menu();
        //printRoomAndRates();  // FR1
        //createReservation();  // FR2
        //reservationChange();  // FR3
        //reservationCancellation(); // FR4
        //detailedReservationSearch(); //FR5 --- DONE ---
        //printRevenue();               //FR6
    }

    private void createC() throws Exception {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String sql = "SELECT * FROM lab7_rooms";

            // Step 3: (omitted in this example) Start transaction

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)){

                while (rs.next()) {
                    String roomCode = rs.getString("RoomCode");
                    String roomName = rs.getString("RoomName");
                    float rate = rs.getFloat("basePrice");
                    System.out.format("%s %s ($%.2f) %n", roomCode, roomName, rate);
                }

            }

            /*try(Statement stmt = conn.createStatement()){
                // Step 4: Send SQL statement to DBMS
                boolean exRes = stmt.execute(sql);
                // Step 5: Handle results
                System.out.format("Result from Select: %b %n", exRes);
            }*/
        }
    }

    private void menu(){
        String ans = "";
        int num = 0;
        menuWords();

        while(num != 7){
            num = 0;
            ans = scannerIn.nextLine();

            try{
                num = Integer.parseInt(ans);

                switch (num) {
                    case 1 -> printRoomAndRates();
                    case 2 -> createReservation();
                    case 3 -> reservationChange();
                    case 4 -> reservationCancellation();
                    case 5 -> detailedReservationSearch();
                    case 6 -> revenue();
                }

                if(num > 0 && num < 7){
                    System.out.println("---------------------------------------------");
                    System.out.println("Press enter to continue");
                    scannerIn.nextLine();
                    menuWords();
                }
            }
            catch (Exception e){
                System.out.println("Invalid input!");
            }
        }
    }


    private void printRoomAndRates() {
        System.out.println("---------------------------------------------");
        System.out.println("Printing Rooms and Rates");
        System.out.println("---------------------------------------------");
        printPopularity();
    }

    private int createReservation(){
        System.out.println("---------------------------------------------");
        System.out.println("Creating a Reservation");
        System.out.println("---------------------------------------------");
        System.out.println("Enter q to quit at any time");

        BasicQuery bQuery = new BasicQuery(scannerIn);

        if(bQuery.getRoomCode().equalsIgnoreCase("")){
            System.out.println("Looking for five reservations");
            BasicQuery bQ = createFiveReservation(bQuery);
            if(bQ != null){
                System.out.println("Reservation is possible");
                insertReservation(bQ);
                System.out.println("Reservation Added");
            }
        }
        else{
            boolean isPossible = checkIfPossible(bQuery);
            if(isPossible){
              insertReservation(bQuery);
            }
            else{
                System.out.println("Looking for five reservations");
                BasicQuery bQ = createFiveReservation(bQuery);
                if(bQ != null){
                    insertReservation(bQ);
                }
            }
        }

        return 0;
    }

    private void reservationChange(){
        System.out.println("---------------------------------------------");
        System.out.println("Changing Reservation Menu");
        System.out.println("---------------------------------------------");
        BasicQuery bQuery = new BasicQuery(scannerIn);
        bQuery.FR3();

        updateReservation(bQuery);

    }

    private void reservationCancellation(){
        System.out.println("---------------------------------------------");
        System.out.println("Cancelling Reservation");
        System.out.println("---------------------------------------------");
        BasicQuery bQuery = new BasicQuery(scannerIn);
        bQuery.setReservationCODE();

        System.out.print("Are you sure you want to delete reservation " + bQuery.getReservationCode() + "? y/n: ");

        String ans = "";
        while(!ans.equals("n") && !ans.equals("y")){
            ans = scannerIn.nextLine();
        }

        System.out.println("---------------------------------------------");
        if(ans.equals("y")){
            boolean ret = deleteReservation(bQuery);

            if(ret) {
                System.out.println("Reservation " + bQuery.getReservationCode() + " Cancelled");
            }
            else {
                System.out.println("Deletion Cancelled");
            }
        }
        else{
            System.out.println("Deletion Cancelled");
        }
    }

    private int detailedReservationSearch() {
        System.out.println("---------------------------------------------");
        System.out.println("Reservation Detailed Overview");
        System.out.println("---------------------------------------------");

        BasicQuery bQuery = new BasicQuery(scannerIn);
        bQuery.FR5();
        String sqlSelect = "SELECT * FROM lab7_reservations";
        List<Object> list = new ArrayList<>();
        StringBuilder whereString = createWhereStringFR5(bQuery, list);

        String totalString = sqlSelect + whereString;

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {


            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(totalString)) {

                int i = 1;
                for (Object p : list) {
                    pstmt.setObject(i++, p);
                }


                try (ResultSet rs = pstmt.executeQuery()) {
                    i = 0;
                    while (rs.next()) { // PopScore, RoomCode,RoomName,Beds, bedType, maxOcc, basePrice, decor, CheckOut,NextCheckIn
                        int CODE = rs.getInt("CODE");
                        String Room = rs.getString("Room");
                        String checkOut = rs.getString("CheckOut");
                        String checkIn = rs.getString("CheckIn");

                        System.out.println("--------------");
                        System.out.format("\nRes. Code: %s", CODE);
                        System.out.format("\nRoom Code: %s", Room);
                        System.out.format("\nCheck In: %s", checkIn);
                        System.out.format("\nCheckOut: %s\n", checkOut);
                        i++;
                    }


                } catch (SQLException e) {
                    e.getStackTrace();
                }

            } catch (SQLException e) {
                e.getStackTrace();
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return 0;
    }

    private void revenue(){
        System.out.println("---------------------------------------------");
        System.out.println("Printing Year Revenue");
        System.out.println("---------------------------------------------");
        printRevenue();
    }

    private boolean checkIfPossible(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {


            String with = "WITH maxCheckIn AS (\n" +
                    "    SELECT max(CheckIn) AS maxI FROM lab7_reservations";

            String maxCheckOut = "), maxCheckOut AS (\n" +
                    "    SELECT max(CheckOut) AS maxO FROM lab7_reservations JOIN maxCheckIn \n" +
                    "    WHERE maxI = CheckIn";
            String selectMaxSql = ")\n" +
                    "SELECT CheckIn, CheckOut FROM maxCheckIn JOIN maxCheckOut JOIN lab7_reservations\n" +
                    "WHERE CheckIn = maxI AND Checkout = maxO";


            with += " WHERE Room = ? ";
            maxCheckOut += " AND Room = ? ";
            selectMaxSql += " AND Room = ? ";

            String totalSql = with + maxCheckOut + selectMaxSql;

            // Step 3: Start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(totalSql)) {
                pstmt.setString(1, bQuery.getRoomCode());
                pstmt.setString(2,bQuery.getRoomCode());
                pstmt.setString(3,bQuery.getRoomCode());

                try(ResultSet rs = pstmt.executeQuery()){
                   if(rs.next()){
                        Date checkIn = rs.getDate("CheckIn");
                        Date checkOut = rs.getDate("CheckOut");
                        System.out.println(checkIn+ " " +checkOut);
                   }
                }
                catch (SQLException e){
                    e.getStackTrace();
                    return false;
                }
            }
            conn.commit();
        }
        catch (SQLException e){
            e.getStackTrace();
            return false;
        }

        return true;
    }

    private void insertReservation(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String insertSql = "INSERT INTO lab7_reservations ('CODE', 'Room','CheckIn','Checkout','Rate','LastName','FirstName','Adults','Kids')\n" +
                    "VALUES (?,?,?,?,?,?,?,?,?)";

            int nexNum = getMaxInt() + 1;
            float roomRate = getRate(bQuery.getRoomCode());

            if(nexNum < 0 || roomRate < 0){
                System.out.println("Invalid RoomCode");
                return;
            }

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, nexNum);
                pstmt.setString(2, bQuery.getRoomCode());
                pstmt.setDate(3, bQuery.getCheckIn());
                pstmt.setDate(4, bQuery.getCheckOut());
                pstmt.setFloat(5,roomRate); // find rate with rooms
                pstmt.setString(6, bQuery.getLastName());
                pstmt.setString(7, bQuery.getFirstName());
                pstmt.setInt(8, bQuery.getNumAdults());
                pstmt.setInt(9, bQuery.getNumChildren());

                try(ResultSet rs = pstmt.executeQuery()){

                    int CODE_r = rs.getInt("CODE");
                    String roomCode = rs.getString("Room");
                    String checkIn = rs.getString("CheckIn");
                    String checkOut = rs.getString("Checkout");
                    float rate = rs.getFloat("Rate");
                    int lastname = rs.getInt("LastName");
                    int firstname = rs.getInt("FirstName");
                    int adults = rs.getInt("Adults");
                    int children= rs.getInt("Kids");

                    //float totalRate = getTotalRate(checkIn,checkOut, rate);

                    System.out.println("Reservation Code: " + CODE_r);
                    System.out.println("Room Code: " + roomCode);
                    System.out.println("Check In: " + checkIn);
                    System.out.println("Check Out: " + checkOut);
                    System.out.println("Full Name: " + lastname + " " + firstname);
                    System.out.println("Num Adults: "+ adults);
                    System.out.println("Num Children: " + children);
                    System.out.format("Rate: %.2f",rate);
                    System.out.println("Total Rate: "); // need to figure out the rate;

                }
                catch (SQLException e){
                    e.getStackTrace();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        }
        catch (SQLException e){
            e.getStackTrace();
        }


    }

    private void updateReservation(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            List<Object> list = new ArrayList<>();
            String updateSql = "UPDATE lab7_reservations ";
            StringBuilder setString = createSetStringFR3(bQuery,list);

            String totalString = updateSql + setString + "WHERE 'CODE' = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(totalString)) {

                int i = 1;
                for (Object p : list) {
                    pstmt.setObject(i++, p);
                }


                try(ResultSet rs = pstmt.executeQuery()){

                    int CODE_r = rs.getInt("CODE");
                    String roomCode = rs.getString("Room");
                    String checkIn = rs.getString("CheckIn");
                    String checkOut = rs.getString("Checkout");
                    float rate = rs.getFloat("Rate");
                    int lastname = rs.getInt("LastName");
                    int firstname = rs.getInt("FirstName");
                    int adults = rs.getInt("Adults");
                    int children= rs.getInt("Kids");

                    System.out.println("Reservation Code: " + CODE_r);
                    System.out.println("Room Code: " + roomCode);
                    System.out.println("Check In: " + checkIn);
                    System.out.println("Check Out: " + checkOut);
                    System.out.println("Full Name: " + lastname + " " + firstname);
                    System.out.println("Num Adults: "+ adults);
                    System.out.println("Num Children: " + children);
                    System.out.format("Rate: %.2f",rate);

                }
                catch (SQLException e){
                    e.getStackTrace();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        }
        catch (SQLException e){
            e.getStackTrace();
        }
    }

    private boolean deleteReservation(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String deleteSql = "DELETE FROM lab7_reservations WHERE CODE = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, bQuery.getReservationCode());
                int rowCount = pstmt.executeUpdate();
                conn.commit();

                if(rowCount > 0){
                    return true;
                }

            } catch (SQLException e) {
                conn.rollback();
            }
        }
        catch (SQLException e){
            e.getStackTrace();
        }
        return false;
    }


    // PRINTS
    private void printPopularity(){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String sql = "WITH maxCheckOut AS(\n" +
                    "    SELECT Room, max(checkout) as maxC FROM lab7_reservations \n" +
                    "    WHERE checkOut <=  CURRENT_DATE()\n" +
                    "    GROUP BY Room\n" +
                    "), maxDays AS (\n" +
                    "    SELECT maxCheckOut.Room, MAX(DATEDIFF(Checkout, CheckIn)) AS maxD FROM lab7_reservations JOIN maxCheckOut ON maxCheckOut.Room = lab7_reservations.Room\n" +
                    "    WHERE maxC = CheckOut \n" +
                    "    GROUP BY Room\n" +
                    "),popDay AS (\n" +
                    "    SELECT SUBDATE( CURRENT_DATE(), INTERVAL 180 DAY) AS Day \n" +
                    "),\n" +
                    "popTable AS (\n" +
                    "    SELECT Room, IF(CheckIn IS NULL, CURRENT_DATE(), CheckIn) AS ChIn, IF(CheckOut IS NULL, CURRENT_DATE(), CheckOut) AS ChOut\n" +
                    "    FROM lab7_reservations LEFT OUTER JOIN lab7_rooms ON roomCode = Room JOIN  popDay\n" +
                    "    WHERE CheckIn >= Day\n" +
                    "), donePopTable AS (\n" +
                    "    SELECT Room, SUM(DATEDIFF(LEAST(CURRENT_DATE(), ChOut ), ChIn))/180 as PopScore, IF(SUBDATE(MAX(ChOut), INTERVAL 1 DAY) < CURRENT_Date(), CURRENT_DATE(), SUBDATE(MAX(ChOut), INTERVAL 1 DAY)) AS NextCheckIn FROM popTable\n" +
                    "    GROUP BY Room\n" +
                    "    ORDER BY PopScore DESC\n" +
                    "), doneMaxTable AS(\n" +
                    "    SELECT maxDays.Room, maxD, maxC, CheckIn, CheckOut FROM maxDays JOIN maxCheckOut ON maxDays.Room = maxCheckOut.Room \n" +
                    "    JOIN lab7_reservations ON maxDays.Room = lab7_reservations.Room \n" +
                    "    WHERE maxD = DATEDIFF(Checkout, CheckIn) AND maxC = CheckOut \n" +
                    "    GROUP BY maxDays.Room,maxD, maxC, CheckIn, CheckOut\n" +
                    ")\n" +
                    "SELECT PopScore, maxD, RoomCode,RoomName,Beds, bedType, maxOcc, basePrice, decor, CheckOut,NextCheckIn\n" +
                    " FROM doneMaxTable JOIN donePopTable ON doneMaxTable.Room = donePopTable.Room JOIN lab7_rooms \n" +
                    "WHERE RoomCode = doneMaxTable.Room\n" +
                    "ORDER BY PopScore DESC";

                conn.setAutoCommit(false);

                try(PreparedStatement pstmt = conn.prepareStatement(sql)){

                    try(ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        System.out.println("Rooms sorted by Popularity Score:");
                        while (rs.next()) { // PopScore, RoomCode,RoomName,Beds, bedType, maxOcc, basePrice, decor, CheckOut,NextCheckIn
                            float PopScore = rs.getFloat("PopScore");
                            int maxD = rs.getInt("maxD");
                            String RoomCode = rs.getString("RoomCode");
                            String RoomName = rs.getString("RoomName");
                            String bedType = rs.getString("bedType");
                            int beds = rs.getInt("beds");
                            int maxOcc = rs.getInt("maxOcc");
                            float basePrice = rs.getFloat("basePrice");
                            String decor = rs.getString("decor");
                            String checkOut = rs.getString("CheckOut");
                            String nextCheckIn = rs.getString("NextCheckIn");

                            System.out.println("--------------");
                            System.out.format("[%d] PopScore: %.2f", i, PopScore);
                            System.out.format("\nRoomCode: %s", RoomCode);
                            System.out.format("\nRoomName: %s", RoomName);
                            System.out.format("\nMax Occupied: %d", maxOcc);
                            System.out.format("\nTotal Beds: %d", beds);
                            System.out.format("\nBed Type: %s", bedType);
                            System.out.format("\nBase Price: %.2f", basePrice);
                            System.out.format("\nDecor: %s", decor);
                            System.out.format("\nDays of Last Stay: %d", maxD);
                            System.out.format("\nLast CheckOut: %s", checkOut);
                            System.out.format("\nNext CheckIn: %s\n", nextCheckIn);
                            i++;

                        }
                        conn.commit();
                    }
                    catch (SQLException e){
                        conn.rollback();
                        e.getStackTrace();
                    }
                }
                catch (SQLException e){
                    conn.rollback();
                    e.getStackTrace();
                }
            }
        catch (SQLException e){
            e.getStackTrace();
        }
    }
    private boolean printReservation(String CODE) {

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectSql = "SELECT * FROM lab7_reservations WHERE CODE = ?";

            // Step 3: Start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, CODE);
                try(ResultSet rs = pstmt.executeQuery()){

                    int CODE_r = rs.getInt("CODE");
                    String roomCode = rs.getString("Room");
                    String checkIn = rs.getString("CheckIn");
                    String checkOut = rs.getString("Checkout");
                    float rate = rs.getFloat("Rate");
                    int lastname = rs.getInt("LastName");
                    int firstname = rs.getInt("FirstName");
                    int adults = rs.getInt("Adults");
                    int children= rs.getInt("Kids");

                    System.out.println("Reservation Code: " + CODE_r);
                    System.out.println("Room Code: " + roomCode);
                    System.out.println("Check In: " + checkIn);
                    System.out.println("Check Out: " + checkOut);
                    System.out.println("Full Name: " + lastname + " " + firstname);
                    System.out.println("Num Adults: "+ adults);
                    System.out.println("Num Children: " + children);
                    System.out.format("Rate: %.2f",rate);

                }
                catch (SQLException e){
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
            }
        }
        catch (SQLException e){
            e.getStackTrace();
        }

        return false;

    }
    private void printRevenue(){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            List<String> roomCodes = new ArrayList<>();
            createRoomCodeList(roomCodes);

            for(String roomC: roomCodes) {
                String sql = "SELECT Room, MONTH(YearDate) as Month, sum(Rate) AS TotalRevenue FROM yearTable JOIN lab7_reservations\n" +
                        "WHERE DATEDIFF(YearDate, CheckIn) >= 0 AND DATEDIFF(YearDate, CheckOut) < 0 AND Room = " + roomC + " \n" +
                        "GROUP BY Room, MONTH(YearDate)\n" +
                        "ORDER BY Room, MONTH(YearDate)";

                conn.setAutoCommit(false);
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    System.out.println("RoomCode\tJan\tFeb\tMarch\tApr\tJun\tJul\tAug\tSept\tOct\tNov\tDec\tTotal");
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {


                        }
                    } catch (SQLException e) {
                        e.getStackTrace();
                    }
                } catch (SQLException e) {
                    e.getStackTrace();
                }
            }


        }
        catch (SQLException e){
            e.getStackTrace();
        }
    }

    private BasicQuery createFiveReservation(BasicQuery bQuery){
        return null;
    }
    private StringBuilder createSetStringFR3(BasicQuery bQuery, List<Object> list){
        int countParams = 0;
        StringBuilder setString = new StringBuilder("");

        if(!bQuery.getLastName().equalsIgnoreCase("")){
            countParams++;
            setString.append(" LastName = ? ");
            list.add(bQuery.getLastName());
        }
        if(!bQuery.getFirstName().equalsIgnoreCase("")){
            if(countParams > 0){
                setString.append(" AND ");
            }
            setString.append(" FirstName = ? ");
            countParams++;
            list.add(bQuery.getFirstName());
        }
        if(bQuery.getCheckIn().compareTo(BasicQuery.QUIT_DATE) == 0){
            if(countParams > 0){
                setString.append(" AND ");
            }
            setString.append(" CheckIn = ? ");
            countParams++;
            list.add(bQuery.getCheckIn());
        }
        if(bQuery.getCheckOut().compareTo(BasicQuery.QUIT_DATE) == 0){
            if(countParams > 0){
                setString.append(" AND ");
            }
            setString.append(" CheckOut = ? ");
            countParams++;
            list.add(bQuery.getCheckOut());
        }
        if(bQuery.getNumAdults() > -1){
            if(countParams > 0){
                setString.append(" AND ");
            }
            setString.append(" Adults = ? ");
            countParams++;
            list.add(bQuery.getNumAdults());
        }

        if(bQuery.getNumChildren() > -1){
            if(countParams > 0){
                setString.append(" AND ");
            }
            setString.append(" Kids = ? ");
            countParams++;
            list.add(bQuery.getNumChildren());
        }
        if(countParams > 0){
            StringBuilder setWord = new StringBuilder(" SET ");
            setWord.append(setString);
            return setWord;
        }

        return setString;
    }
    private StringBuilder createWhereStringFR5(BasicQuery bQuery, List<Object> list){
        int countParams = 0;
        StringBuilder whereString = new StringBuilder("");

        if(!bQuery.getLastName().equalsIgnoreCase("")){
            countParams++;

            whereString.append("LastName");
            if(bQuery.getLastName().contains("%")){
                whereString.append(" LIKE ");
            }
            else{
                whereString.append(" = ");
            }

            whereString.append(" ? ");
            list.add(bQuery.getLastName());
        }

        if(!bQuery.getFirstName().equalsIgnoreCase("")){
            if(countParams > 0){
                whereString.append(" AND ");
            }

            whereString.append("FirstName");
            if(bQuery.getLastName().contains("%")){
                whereString.append(" LIKE ");
            }
            else{
                whereString.append(" = ");
            }

            whereString.append(" ? ");
            list.add(bQuery.getFirstName());
            countParams++;
        }

        if(bQuery.getCheckIn().compareTo(BasicQuery.EMPTY_DATE) != 0){
            if(countParams > 0){
                whereString.append(" AND ");
            }

            whereString.append("CheckIn = ? ");
            list.add(bQuery.getCheckIn());
            countParams++;
        }

        if(bQuery.getCheckOut().compareTo(BasicQuery.EMPTY_DATE) != 0){
            if(countParams > 0){
                whereString.append(" AND ");
            }
            whereString.append("CheckOut = ? ");
            list.add(bQuery.getCheckOut());
            countParams++;
        }

        if(!bQuery.getRoomCode().equalsIgnoreCase("")){
            if(countParams > 0){
                whereString.append(" AND ");
            }

            whereString.append("RoomCode");
            if(bQuery.getRoomCode().contains("%")){
                whereString.append(" LIKE ");
            }
            else{
                whereString.append(" = ");
            }

            whereString.append(" ? ");
            list.add(bQuery.getRoomCode());
            countParams++;
        }

        if(bQuery.getReservationCode() > 0){
            if(countParams > 0){
                whereString.append(" AND ");
            }

            whereString.append("CODE = ? ");
            list.add(bQuery.getReservationCode());
            countParams++;
        }

        if(countParams > 0){
            StringBuilder whereWord = new StringBuilder(" WHERE ");
            whereWord.append(whereString);
            return whereWord;
        }

        return whereString;
    }

    private int getMaxInt() {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectMax = "SELECT max(CODE) FROM lab7_reservations";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectMax)) {

                try(ResultSet rs = pstmt.executeQuery()){
                    return rs.getInt("CODE");

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
    private float getRate(String roomCode) {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectRate = "SELECT rate FROM lab7_rooms WHERE Room = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectRate)) {
                pstmt.setString(1,roomCode);

                try(ResultSet rs = pstmt.executeQuery()){
                    return rs.getFloat("Rate");

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
    private float getTotalRate(Date start, Date end, float baseRate){
        int days = getNumOfWeeks(start,end);
        int weeks = days / 7;
       // String startDay = getDay(start);
       // String endDay = getDay(end);

        int saturdays = 0;
        int sundays = 0;

        return 0.f;
    }

    private int getNumOfWeeks(Date start, Date end){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String dateSql = "SELECT DATEDIFF(?, ?) AS DateDiff; ";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(dateSql)) {
                pstmt.setDate(1,end);
                pstmt.setDate(2,start);

                try(ResultSet rs = pstmt.executeQuery()){
                    return rs.getInt("DateDiff");
                }
                catch (SQLException e){
                    e.getStackTrace();
                    conn.rollback();
                }
                conn.commit();
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
    private void createRoomCodeList(List<String> roomC){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String sql = "SELECT RoomCode FROM lab7_rooms";

            conn.setAutoCommit(false);

            try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try(ResultSet rs = pstmt.executeQuery()) {
                    while(rs.next()){
                        String roomCode = rs.getString("RoomCode");
                        roomC.add(roomCode);
                    }
                }
                catch (SQLException e){
                    conn.rollback();
                    e.getStackTrace();
                }
                conn.commit();
            }
            catch (SQLException e){
                e.getStackTrace();
            }


        }
        catch (SQLException e){
            e.getStackTrace();
        }
    }

    private void menuWords(){
        System.out.println("Lab 7: Inn Reservations");
        System.out.println("[ 1 ] Rooms and Rates");
        System.out.println("[ 2 ] Reservations");
        System.out.println("[ 3 ] Reservation Change");
        System.out.println("[ 4 ] Reservation Cancellation");
        System.out.println("[ 5 ] Detailed Information");
        System.out.println("[ 6 ] Revenue");
        System.out.println("[ 7 ] Quit");
        System.out.println("---------------------------------------------");
    }
    private static final Scanner scannerIn = new Scanner(System.in);
}
