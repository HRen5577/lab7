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
    public InnReservations(){

        menu();
        //printRoomAndRates();        
        //createReservation();          
        //reservationChange();          
        //reservationCancellation();    
        //detailedReservationSearch();  
        //printRevenue();              
    }

    private void menu(){
        int num = 0;
        menuWords();

        while(num != 7){
            num = 0;
            String ans = scannerIn.nextLine();

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

    private void createReservation(){
        System.out.println("---------------------------------------------");
        System.out.println("Creating a Reservation");
        System.out.println("---------------------------------------------");
        System.out.println("Enter q to quit at any time");

        BasicQuery bQuery = new BasicQuery(scannerIn);
        bQuery.FR2();

        if(bQuery.getRoomCode().equalsIgnoreCase("")){
            System.out.println("Looking for five reservations...");
            BasicQuery bQ = createFiveReservation(bQuery);

            if(bQ != null){
                insertReservation(bQ);
            } else {
                return;
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

    private void detailedReservationSearch() {
        System.out.println("---------------------------------------------");
        System.out.println("Reservation Detailed Overview");
        System.out.println("---------------------------------------------");

        BasicQuery bQuery = new BasicQuery(scannerIn);
        bQuery.FR5();
        String sqlSelect = "SELECT * FROM hrendon.lab7_reservations";
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
                    while (rs.next()) {
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

            // CHECK IF SAME BEDTYPE
            String RoomBedType = getBedType(bQuery.getRoomCode());
            if(!bQuery.getBedType().equalsIgnoreCase("") && !bQuery.getBedType().equalsIgnoreCase(RoomBedType)){
                return false;
            }

            // CHECK IF DAYS OVERLAP
            conn.setAutoCommit(false);

            String select = "SELECT Room, max(CheckOut) AS MostRecentCheckOut FROM hrendon.lab7_reservations\n" +
                    "WHERE Room = ?\n" +
                    "GROUP BY Room";

            try (PreparedStatement pstmt = conn.prepareStatement(select)) {
                pstmt.setString(1, bQuery.getRoomCode());

                try(ResultSet rs = pstmt.executeQuery()){
                   if(rs.next()){
                        Date recentCheckOut = rs.getDate("MostRecentCheckOut");

                        if(bQuery.getCheckIn().compareTo(recentCheckOut) <= 0){
                            return true;
                        }
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

            String insertSql = "INSERT INTO hrendon.lab7_reservations (CODE, Room,CheckIn,Checkout,Rate,LastName,FirstName,Adults,Kids)\n" +
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
                pstmt.setFloat(5,roomRate);
                pstmt.setString(6, bQuery.getLastName());
                pstmt.setString(7, bQuery.getFirstName());
                pstmt.setInt(8, bQuery.getNumAdults());
                pstmt.setInt(9, bQuery.getNumChildren());

                pstmt.executeUpdate();

                float totalRate = getTotalRate(bQuery.getCheckIn(),bQuery.getCheckOut(), roomRate);

                System.out.println("Reservation Code: " + nexNum);
                System.out.println("Room Code: " + bQuery.getRoomCode());
                System.out.println("Check In: " + bQuery.getCheckIn());
                System.out.println("Check Out: " + bQuery.getCheckOut());
                System.out.println("Full Name: " + bQuery.getLastName() + " " + bQuery.getFirstName());
                System.out.println("Num Adults: "+ bQuery.getNumAdults());
                System.out.println("Num Children: " + bQuery.getNumChildren());
                System.out.format("Rate: %.2f",roomRate);
                System.out.format("\nTotal Rate: %.2f\n ", totalRate);
                System.out.println("---------------------------------------------");
                System.out.println("0 - Confirm   or   1 - Cancel");
                Integer req = Integer.parseInt(scannerIn.nextLine());
                if (req == 1) {
                    System.out.println("Reservation cancelled");
                    return;
                }
                conn.commit();
            } catch (SQLException e) {
                System.out.println("error in prepare update");
                conn.rollback();
            }
        }
        catch (SQLException e){
            System.out.println("error in update connection");
            e.getStackTrace();
        }
        System.out.println("Reservation Added");
    }

    private void updateReservation(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            List<Object> list = new ArrayList<>();

            String updateSql = "UPDATE hrendon.lab7_reservations ";
            StringBuilder setString = createSetStringFR3(bQuery,list);

            if(list.isEmpty()){
                System.out.println("No Changes made!");
                return;
            }

            boolean ret =  checkDateConflict(bQuery);

            if(!ret){
                return;
            }

            String totalString = updateSql + setString + "WHERE CODE = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(totalString)) {
                int i = 1;
                for (Object p : list) {
                    pstmt.setObject(i++, p);
                }

                int rows  = pstmt.executeUpdate();

                if(rows != 1) {
                    conn.rollback();
                    return;
                }
                conn.commit();
                System.out.println("Reservation " + bQuery.getReservationCode() + " Updated!" );
            } catch (SQLException e) {
                conn.rollback();
            }
        }
        catch (SQLException e){
            e.getStackTrace();
        }

        printReservation(bQuery.getReservationCode());

    }
    private boolean deleteReservation(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String deleteSql = "DELETE FROM hrendon.lab7_reservations WHERE CODE = ?";

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
            conn.commit();
        }
        catch (SQLException e){
            e.getStackTrace();
        }
        return false;
    }

    private boolean checkDateConflict(BasicQuery bQuery){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            conn.setAutoCommit(false);

            String totalString = "SELECT * FROM hrendon.lab7_reservations WHERE CODE = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(totalString)) {
                pstmt.setObject(1, bQuery.getReservationCode());

                try (ResultSet rs = pstmt.executeQuery()) {
                    conn.commit();

                    if(rs.next()){
                        String Room = rs.getString("Room");
                        Date checkOut = rs.getDate("CheckOut");
                        Date checkIn = rs.getDate("CheckIn");
                        
                        return checkSelect(bQuery, Room, checkOut, checkIn);

                    }
                    else{
                        return false;
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    e.getStackTrace();
                }

            } catch (SQLException e) {
                e.getStackTrace();
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;

    }

    private boolean checkSelect(BasicQuery bQuery, String Room, Date checkOut, Date checkIn) {
        Date cO = checkOut;
        Date cI = checkIn;

        if (bQuery.getCheckOut().compareTo(BasicQuery.EMPTY_DATE) != 0) {
            cO = bQuery.getCheckOut();
        }

        if (bQuery.getCheckIn().compareTo(BasicQuery.EMPTY_DATE) != 0) {
            cI = bQuery.getCheckIn();
        }

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            conn.setAutoCommit(false);

            String totalString = "select * from hrendon.lab7_rooms rm1 \n" +
                            "join hrendon.lab7_rooms rm2 on ? <> rm1.code and ? = rm2.room \n" +
                            "where ? <= rm2.checkin and rm1 and ? > ?";

            try (PreparedStatement pstmt = conn.prepareStatement(totalString)) {
                pstmt.setInt(1,bQuery.getReservationCode());
                pstmt.setString(2,Room);
                pstmt.setDate(3,cO);
                pstmt.setDate(4,cO);
                pstmt.setDate(5,cI);
                
                
                try (ResultSet rs = pstmt.executeQuery()){
                    if (rs.next()) {
                        return false;
                    }
                    return true;
                } catch (SQLException e) {
                    e.getStackTrace();
                }


            } catch (SQLException e) {
                e.getStackTrace();
            }
        } catch (SQLException e) {
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
                    "    SELECT Room, max(checkout) as maxC FROM hrendon.lab7_reservations \n" +
                    "    WHERE checkOut <=  CURRENT_DATE()\n" +
                    "    GROUP BY Room\n" +
                    "), maxDays AS (\n" +
                    "    SELECT maxCheckOut.Room, MAX(DATEDIFF(Checkout, CheckIn)) AS maxD FROM hrendon.lab7_reservations JOIN maxCheckOut ON maxCheckOut.Room = hrendon.lab7_reservations.Room\n" +
                    "    WHERE maxC = CheckOut \n" +
                    "    GROUP BY Room\n" +
                    "),popDay AS (\n" +
                    "    SELECT SUBDATE( CURRENT_DATE(), INTERVAL 180 DAY) AS DayAgo \n" +
                    "),popTable AS (\n" +
                    "    SELECT Room, IF(CheckIn IS NULL, CURRENT_DATE(), CheckIn) AS ChIn, IF(CheckOut IS NULL, CURRENT_DATE(), CheckOut) AS ChOut\n" +
                    "    FROM hrendon.lab7_reservations LEFT OUTER JOIN hrendon.lab7_rooms ON roomCode = Room JOIN  popDay\n" +
                    "    WHERE (CheckIn >= DayAgo AND Checkout < CURRENT_DATE()) OR ( DayAgo BETWEEN CheckIn AND CheckOut )\n" +
                    "), donePopTable AS (\n" +
                    "    SELECT Room, SUM(DATEDIFF(LEAST(CURRENT_DATE(), ChOut ), GREATEST(ChIn,DayAgo)))/180 as PopScore FROM popTable JOIN popDay\n" +
                    "    GROUP BY Room\n" +
                    "    ORDER BY PopScore DESC\n" +
                    "), doneMaxTable AS(\n" +
                    "    SELECT maxDays.Room, maxD, maxC, CheckIn, CheckOut FROM maxDays JOIN maxCheckOut ON maxDays.Room = maxCheckOut.Room \n" +
                    "    JOIN hrendon.lab7_reservations ON maxDays.Room = hrendon.lab7_reservations.Room \n" +
                    "    WHERE maxD = DATEDIFF(Checkout, CheckIn) AND maxC = CheckOut \n" +
                    "    GROUP BY maxDays.Room,maxD, maxC, CheckIn, CheckOut\n" +
                    "), TotalMaxCheckOut AS (\n" +
                    "    SELECT RoomCode AS RC, MAX(Checkout) AS TMaxCheckout FROM hrendon.lab7_rooms LEFT OUTER JOIN hrendon.lab7_reservations ON Room = RoomCode\n" +
                    "    GROUP BY RoomCode\n" +
                    ")\n" +
                    "SELECT IF(PopScore IS null OR PopScore < 0,0,PopScore) AS PopScore, IF(maxD IS null,0,maxD) AS maxD, RoomCode,RoomName,Beds, bedType, maxOcc, basePrice, decor, IF(CheckOut IS null,'No Last CheckOut', CheckOut) AS CheckOut, IF(DATEDIFF(TMaxCheckout, CURRENT_DATE) <= 0 OR TMaxCheckout IS null,CURRENT_DATE,TMaxCheckout) AS NextCheckIn FROM donePopTable LEFT OUTER JOIN doneMaxTable ON doneMaxTable.Room = donePopTable.Room RIGHT OUTER JOIN hrendon.lab7_rooms ON RoomCode = donePopTable.Room\n" +
                    "    JOIN TotalMaxCheckOut ON RC = RoomCode\n" +
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
                            System.out.format("\nDays occupied of Last Stay: %d", maxD);
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
    private void printReservation(int CODE) {

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectSql = "SELECT * FROM hrendon.lab7_reservations WHERE CODE = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, CODE);
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
                    conn.rollback();
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
    private void printRevenue(){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            List<String> roomCodes = new ArrayList<>();
            createRoomCodeList(roomCodes);
            System.out.println("RoomCode\tJan\t\t\t\tFeb\t\t\t\tMarch\t\t\tApr\t\t\t\tMay\t\t\t\tJun\t\t\t\tJul\t\t\t\tAug\t\t\t\tSept\t\t\tOct\t\t\t\tNov\t\t\t\tDec\t\t\t\tTotal");

            for(String roomC: roomCodes) {
                String sql = "SELECT Room, MONTH(YearDate) as Month, sum(Rate) AS TotalRevenue FROM hrendon.yearTable JOIN hrendon.lab7_reservations\n" +
                        "WHERE DATEDIFF(YearDate, CheckIn) >= 0 AND DATEDIFF(YearDate, CheckOut) < 0 AND Room = ? \n" +
                        "GROUP BY Room, MONTH(YearDate)\n" +
                        "ORDER BY Room, MONTH(YearDate)";

                conn.setAutoCommit(false);
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1,roomC);
                    try (ResultSet rs = pstmt.executeQuery()) {

                        int totalMonths = 0;
                        float yearTotal = 0;

                        System.out.print(roomC);
                        while (rs.next()) {
                            float totalR = rs.getFloat("TotalRevenue");
                            int monthNum = rs.getInt("Month");
                            yearTotal += totalR;
                            totalMonths++;

                            if(monthNum > totalMonths){
                                while(monthNum > totalMonths){
                                    totalMonths++;
                                    System.out.format("\t\t\t0.00");
                                }
                            }

                            System.out.format("\t\t\t%.2f", totalR);
                        }
                        while(totalMonths < 12){
                            System.out.format("\t\t\t0.00");
                            totalMonths++;
                        }
                        System.out.format("\t\t\t%.2f",yearTotal);
                        System.out.println();
                    } catch (SQLException e) {
                        conn.rollback();
                        e.getStackTrace();
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    e.getStackTrace();
                }
            }
            conn.commit();
        }
        catch (SQLException e){
            e.getStackTrace();
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private StringBuilder createWhereStringFR2(BasicQuery bQuery){
        StringBuilder where = new StringBuilder();
        Integer totalOcc = bQuery.getNumAdults() + bQuery.getNumChildren();
        where.append("where maxOcc >= " + totalOcc);
        if(!bQuery.getBedType().equalsIgnoreCase("")) {
            where.append(" and BedType = '" + bQuery.getBedType() + "'");
        }

        return where;
    }


    private BasicQuery createFiveReservation(BasicQuery bQuery){
        Integer count = 1;
        System.out.println("---------------------------------------------");
        System.out.println("Select preferences from 1 to -");
        System.out.println("---------------------------------------------");

        StringBuilder sqlWhere = createWhereStringFR2(bQuery);

        StringBuilder sql = new StringBuilder();
        sql.append("With Oset as (select distinct Room, if(CheckIn <= '");
        sql.append(bQuery.getCheckOut());
        sql.append("' and CheckOut > '");
        sql.append(bQuery.getCheckIn());
        sql.append("', 'Occupied', 'Empty') as occupied from hrendon.lab7_reservations as rv) ");
        sql.append("select rv.Room, rm.RoomName, rm.Beds, rm.maxOcc, rm.basePrice, rm.decor " +
                    "from hrendon.lab7_reservations as rv " + 
                    "join hrendon.lab7_rooms as rm on rm.RoomCode = rv.Room " +
                    "join Oset as o on o.Room = rv.Room ");
        sql.append(sqlWhere);
        sql.append(" group by rv.Room having max(Occupied) = 'Empty' order by rv.Room limit 5;");

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
                conn.setAutoCommit(false);
            try (PreparedStatement s = conn.prepareStatement(sql.toString())) {
                try (ResultSet rs = s.executeQuery()) {
                    System.out.println("Code, Room name, bed type, number of beds, price, style \n");
                    while (rs.next()) {
                        String row = "";
                        for (int i = 1; i <= 6; i++) {
                            row += rs.getString(i) + ", ";          
                        }
                        System.out.println(count + ". " + row + "\n");
                        count++;
                    }
                    System.out.println("---------------------------------------------");
                    System.out.print("Room selection (enter 0 to cancel): ");
                    Integer selection = Integer.parseInt(scannerIn.nextLine());
                    if(selection == 0){
                        return null;
                    }
                    rs.absolute(selection);
                    bQuery.newRoomCode(rs.getString(1));
                } catch (SQLException e) {
                    System.out.println("error in rs execute");
                    e.getStackTrace();
                }
            } catch (SQLException e) {
                System.out.println("error in s prepare");
            }
        } catch (SQLException e) {
            System.out.println("error in get five rooms");
            e.getStackTrace();
        }

        
        return bQuery;
    }
    private StringBuilder createSetStringFR3(BasicQuery bQuery, List<Object> list){
        int countParams = 0;
        StringBuilder setString = new StringBuilder();

        if(!bQuery.getLastName().equalsIgnoreCase("")){
            countParams++;
            setString.append(" LastName = ? ");
            list.add(bQuery.getLastName());
        }
        if(!bQuery.getFirstName().equalsIgnoreCase("")){
            if(countParams > 0){
                setString.append(" , ");
            }
            setString.append(" FirstName = ? ");
            countParams++;
            list.add(bQuery.getFirstName());
        }
        if(bQuery.getCheckIn().compareTo(BasicQuery.EMPTY_DATE) != 0 ){
            if(countParams > 0){
                setString.append(" , ");
            }
            setString.append(" CheckIn = ? ");
            countParams++;
            list.add(bQuery.getCheckIn());
        }

        if(bQuery.getCheckOut().compareTo(BasicQuery.EMPTY_DATE) != 0){
            if(countParams > 0){
                setString.append(" , ");
            }
            setString.append(" Checkout = ? ");
            countParams++;
            list.add(bQuery.getCheckOut());
        }

        if(bQuery.getNumAdults() > 0){
            if(countParams > 0){
                setString.append(" , ");
            }
            setString.append(" Adults = ? ");
            countParams++;
            list.add(bQuery.getNumAdults());
        }

        if(bQuery.getNumChildren() > 0){
            if(countParams > 0){
                setString.append(" , ");
            }
            setString.append(" Kids = ? ");
            countParams++;
            list.add(bQuery.getNumChildren());
        }
        if(countParams > 0){
            StringBuilder setWord = new StringBuilder(" SET ");
            setWord.append(setString);
            list.add(bQuery.getReservationCode());
            return setWord;
        }

        return setString;
    }
    private StringBuilder createWhereStringFR5(BasicQuery bQuery, List<Object> list){
        int countParams = 0;
        StringBuilder whereString = new StringBuilder();

        if(!bQuery.getLastName().equalsIgnoreCase("")){
            countParams++;

            whereString.append(" LastName ");
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

            whereString.append(" FirstName ");
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

            whereString.append(" CheckIn = ? ");
            list.add(bQuery.getCheckIn());
            countParams++;
        }

        if(bQuery.getCheckOut().compareTo(BasicQuery.EMPTY_DATE) != 0){
            if(countParams > 0){
                whereString.append(" AND ");
            }
            whereString.append(" CheckOut = ? ");
            list.add(bQuery.getCheckOut());
            countParams++;
        }

        if(!bQuery.getRoomCode().equalsIgnoreCase("")){
            if(countParams > 0){
                whereString.append(" AND ");
            }

            whereString.append(" Room ");
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

            whereString.append(" CODE = ? ");
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

            String selectMax = "SELECT max(CODE) AS mCode FROM hrendon.lab7_reservations";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectMax)) {

                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return rs.getInt("mCode");
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
    private float getRate(String roomCode) {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectRate = "SELECT * FROM hrendon.lab7_rooms WHERE RoomCode = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectRate)) {
                pstmt.setString(1,roomCode);

                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return rs.getFloat("basePrice");
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
    private float getTotalRate(Date start, Date end, float baseRate){
        int totalDays = getTotalDays(start,end);
        int totalWeeks = totalDays / 7;

        int weekdays = totalDays;
        int weekends = 0;

        String startDay = getDay(start);
        String endDay = getDay(end);


        if((startDay.equalsIgnoreCase("Sunday") || startDay.equalsIgnoreCase("Saturday")) && (endDay.equalsIgnoreCase("Sunday") || endDay.equalsIgnoreCase("Saturday")) ){
            weekends += 1;
        }

        weekends = weekends + (totalWeeks*2);
        weekdays = weekdays - (weekends);

        return (float) ((baseRate * weekdays) + ((baseRate*.10) + baseRate)*weekends);
    }

    private String getDay(Date start) {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectDate = "SELECT DAYNAME(?) AS day";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectDate)) {
                pstmt.setDate(1, start);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if(rs.next()){
                        return rs.getString("day");
                    }
                }
                catch (SQLException e){
                    e.getStackTrace();
                    conn.rollback();
                }

            } catch (SQLException e) {
                e.getStackTrace();
                conn.rollback();
            }
        }catch (SQLException e ){
            e.getStackTrace();
        }

        return "";

    }
    private String getBedType(String RoomCode){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String selectDate = "SELECT * FROM hrendon.lab7_reservations WHERE Room = ? ";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(selectDate)) {
                pstmt.setString(1, RoomCode);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if(rs.next()){
                        return rs.getString("bedType");
                    }
                }
                catch (SQLException e){
                    e.getStackTrace();
                    conn.rollback();
                }

            } catch (SQLException e) {
                e.getStackTrace();
                conn.rollback();
            }
        }catch (SQLException e ){
            e.getStackTrace();
        }

        return "";

    }

    private int getTotalDays(Date start, Date end){
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            String dateSql = "SELECT DATEDIFF(?, ?) AS DateDiff; ";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(dateSql)) {
                pstmt.setDate(1,end);
                pstmt.setDate(2,start);

                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return rs.getInt("DateDiff");
                    }
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

            String sql = "SELECT RoomCode FROM hrendon.lab7_rooms";

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
