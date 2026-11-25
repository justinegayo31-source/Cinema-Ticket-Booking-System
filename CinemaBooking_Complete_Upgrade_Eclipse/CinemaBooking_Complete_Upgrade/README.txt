CINEMA BOOKING | ECLIPSE JAVA SWING PROJECT
===========================================

REQUIREMENTS
- Eclipse IDE with a JDK (Java 17, 21 or 25)
- No JavaFX, MySQL, extra dependencies or internet connection needed

IMPORT INTO ECLIPSE
1. Extract the ZIP.
2. Eclipse > File > Import > General > Existing Projects into Workspace.
3. Select the extracted CinemaBookingCompleteUpgrade directory (contains .project).
4. Finish. Under src/cinema right-click Main.java > Run As > Java Application.
5. Demo login: admin / admin. Three failed login attempts allowed; the counter is hidden.

If another project with the same name exists, use a new Eclipse workspace or
import the new project after renaming the old project. Keep backups first.

FEATURES
1. Dashboard: active movie count, ticket totals and revenue.
2. Movie management: create/edit/delete movies; genre dropdown; two validated images
   per movie; change poster/banner; movie open/closed status.
3. Screening management: multiple showtimes for one movie, screen assignments,
   active/closed status and independent availability for each showtime.
4. Seat map: 40 seats per screening: A1-C8 Regular, D1-E8 Premium.
   Gold = regular, teal = premium, coral = selected, gray = booked.
5. Checkout: selected-seat list, subtotal, cash, change and confirmation review.
6. Printable ticket receipt (uses system print dialog).
7. Booking history: search by text; status filter; view/print receipt; CSV export.
8. Cancel Booking: entire booking cancelled and seats released; status kept in history.
9. Cancel selected seats: partial cancellation; remaining seats stay booked.
10. Sales reports: tickets and net receipts by movie and date.
11. Movie poster gallery: banner/poster cards and screening information.
12. Dark/light toggle on the dashboard; the five supplied accent colors are retained:
    #E5BA72 #D8905D #C8674F #599BA3 #39312C.
13. ZIP backup/restore for local records and imported images.
14. Local persistence using Java serialization; no database or network required.

CLASSROOM DEMONSTRATION
- Add or edit 3 movies; confirm each has a poster and banner.
- Add a second showtime for one movie from Schedules & Seats.
- Book seats A1, A2 and D1 on the first showtime.
- Deselect/reselect a seat before booking; review and confirm payment.
- Return to the same showtime: those seats are now disabled/Booked.
- Choose the second showtime: A1 is still available.
- Booking History: cancel seat A1 only; A2 and D1 remain reserved.
- Check the booking receipt and history, and inspect Sales Reports.
- Create a ZIP backup, close and reopen the app; records remain.

LOCAL DATA
Records are saved in USER_HOME/.cinema-booking-system/records.dat.
Imported images are copied into USER_HOME/.cinema-booking-system/posters/.
Create a ZIP backup BEFORE importing this upgrade if you have previous bookings.
Older versions' existing records are migrated to screening entries on first launch.

LIMITATIONS
- This is a single-computer classroom demo with hard-coded admin credentials.
  It is NOT suitable for a deployed commercial cinema.
- Transactions are local and use cash-only simulation; no automatic cash refund,
  payment gateway, printed ticket barcode or multi-user seat locking.
- Sales reports show net active ticket value after cancellations, NOT cash refunds.
- Each showtime is a label (e.g. 7:00 PM) rather than a dated calendar session.
  Use distinct screening entries for distinct presentations.
- Only restore trusted ZIP backups from this application. Backups contain customer
  names and transaction details: store them securely.
- Dark/light theme is applied to newly opened windows; toggle returns to dashboard.

TESTING
The optional ProjectSelfTest.java runs local, isolated logic checks.
Right-click ProjectSelfTest.java > Run As > Java Application.
GuiSmokeTest.java optionally produces preview screenshots when run with a GUI.

MAIN ENTRY: src/cinema/Main.java
