package Main;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;

import Database.FetchData;
import Database.WriteExceptionToLog;

public class MainApp {
	
	public static void main(String[] args) {
		new MainApp();
	}
	
	private boolean isLogin;
	private int userId;
	JButton loginButton, logoutButton, personalBookListButton;
	FetchData fetchData;
	
	public MainApp() {
		JFrame mainWindow = new JFrame("Book Broker");
		fetchData = new FetchData();
		// Create components
		JLabel header = addHeader();
		JTextField searchField = addSearchField();
		JComboBox<String> sortBox = addSortBox();
		
		//Opens new login screen with username and password field, clicking enter closes the window
		loginButton = addLoginButton();
		logoutButton = addLogoutButton();
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userId = -1;
				isLogin = false;
				logoutButton.setVisible(false);
				loginButton.setVisible(true);
				personalBookListButton.setVisible(false);
			}
		});
		loginButton.addActionListener(popNewLoginWindow());
		
		personalBookListButton = addPersonalListButton();
		personalBookListButton.addActionListener(popNewPersonalBookWindow());
		
		//Adding the books to the main page, each is responsive and opens a new window with details about the book
		List<Book> originList = fetchData.FetchAllWithoutFilter();
		for (int i = 0; i < originList.size() && i < 5; i++) {
			JButton b = new JButton(originList.get(i).getTitle());
			b.setBounds(20, 100 + i*80, 500, 60);
			b.addActionListener(popNewBookWindow(originList.get(i)));
			mainWindow.add(b);
		}
		
		// Add components to frame
		mainWindow.add(header);
		mainWindow.add(searchField);
		mainWindow.add(sortBox);
		mainWindow.add(loginButton);
		mainWindow.add(logoutButton);
		mainWindow.add(personalBookListButton);
		personalBookListButton.setVisible(false);
		
		mainWindow.setLayout(null);
		mainWindow.setSize(550, 600);
		mainWindow.setVisible(true);
		mainWindow.setResizable(false);
		mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	private Action popNewBookWindow(Book b) {
		return new AbstractAction("open") {
			public void actionPerformed(ActionEvent e) {
				JFrame bookDetail = new JFrame("Book Details");
				
				JLabel bookTitle = new JLabel("Title: " + b.getTitle());
				bookTitle.setBounds(100,50,400,40);
				bookTitle.setFont(new Font("Helvetica", Font.BOLD, 24));
				
				JLabel author = new JLabel("Author: " + b.getAuthor());
				author.setBounds(100,100,400,40);
				author.setFont(new Font("Helvetica", Font.BOLD, 24));
				
				JLabel date = new JLabel("Publish Date: " + b.getPublishDate());
				date.setBounds(100,150,400,40);
				date.setFont(new Font("Helvetica", Font.BOLD, 24));
				
				String websiteLink = "https://en.wikipedia.org/wiki/" + b.getAuthor().replace(' ', '_');
				JLabel link = new JLabel("Link: " + websiteLink);
				link.setBounds(100,200,400,40);
				link.setFont(new Font("Helvetica", Font.BOLD, 15));
				
				JLabel addedMessage = new JLabel("Added to your list.");
				addedMessage.setBounds(100, 380, 200, 75);
				addedMessage.setFont(new Font("Helvetica", Font.PLAIN, 16));
				addedMessage.setVisible(false);
				
				JButton addToPersonalList = new JButton("Add to your personal list");
				addToPersonalList.setBounds(100, 300, 200, 75);
				addToPersonalList.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PersonalBookShelf pbs = new PersonalBookShelf(userId, b.getBookId(), 0);
						if(fetchData.insertData(pbs, "PersonalBookShelf")) {
							System.out.println("Success");
							addedMessage.setVisible(true);
						}
					}
				});
				
				JButton close = new JButton("Close");
				close.setBounds(420, 10, 100, 40);
				close.addActionListener(new AbstractAction("close") {
					public void actionPerformed(ActionEvent e) {
						bookDetail.dispose();
					}
				});
				if(!isLogin) {
					addToPersonalList.setVisible(false);
				}
				
				bookDetail.add(addedMessage);
				bookDetail.add(addToPersonalList);
				bookDetail.add(bookTitle);
				bookDetail.add(date);
				bookDetail.add(link);
				bookDetail.add(author);
				bookDetail.add(close);
				bookDetail.setLayout(null);
				bookDetail.setSize(550, 600);
				bookDetail.setVisible(true);
				bookDetail.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			}
		};
	}
	
	private Action popNewLoginWindow() {
		return new AbstractAction("open") {
			public void actionPerformed(ActionEvent e) {
				JFrame loginWindow = new JFrame("Login");
				JLabel loginHeader = addLoginHeader();
				JTextField user = addUserField();
				JTextField pass = addPassField();
				
				JButton loginEnter = addLoginEnterButton();
				JButton loginRegister = addLoginRegisterButton();
				loginEnter.addActionListener(new AbstractAction("close") {
					public void actionPerformed(ActionEvent e) {
						userId = fetchData.verifyPassword(user.getText(), pass.getText());
						loginWindow.dispose();
						if(userId != -1) {
							isLogin = true;
							logoutButton.setVisible(true);
							loginButton.setVisible(false);
							personalBookListButton.setVisible(true);
						} else {
							logoutButton.setVisible(false);
							loginButton.setVisible(true);
							personalBookListButton.setVisible(false);
						}
							
					}
				});
				loginRegister.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {		
						Login l = new Login(-1, user.getText(), "", pass.getText(), "user");
						if(fetchData.insertData(l, "Login")) {
							userId = fetchData.verifyPassword(user.getText(), pass.getText());
							isLogin = true;
							logoutButton.setVisible(true);
							loginButton.setVisible(false);
							personalBookListButton.setVisible(true);
							loginWindow.dispose();
						}
					}
				});
				
				
				loginWindow.add(loginHeader);
				loginWindow.add(user);
				loginWindow.add(pass);
				loginWindow.add(loginEnter);
				loginWindow.add(loginRegister);
				
				loginWindow.setLayout(null);
				loginWindow.setSize(300, 300);
				loginWindow.setVisible(true);
				loginWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			}
		};
	}
	
	private Action popNewPersonalBookWindow() {
		return new AbstractAction("open") {
			public void actionPerformed(ActionEvent e) {
				JFrame personalBookList = new JFrame("Your Book List");
				JButton close = new JButton("Close");
				JLabel personalListHeader = new JLabel("Your Book List");
				personalListHeader.setBounds(20, 20, 200, 20);
				personalListHeader.setFont(new Font("Helvetica", Font.BOLD, 24));
				
				close.setBounds(420, 10, 100, 40);
				close.addActionListener(new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						personalBookList.dispose();
					}
				});
				List<Book> userBookList = fetchData.FetchByUserId(userId);
				if(userBookList != null) {
					for (int i = 0; i < userBookList.size() && i < 5; i++) {
						JButton b = new JButton(userBookList.get(i).getTitle());
						b.setBounds(20, 100 + i*60, 300, 40);
						b.addActionListener(popNewBookWindow(userBookList.get(i)));
						int bookId = userBookList.get(i).getBookId();
						JButton delete = new JButton("Delete");
						delete.setBounds(350, 100 + i*60, 100, 40);
						delete.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								fetchData.RemoveFromPersonalBookshelf(userId, bookId);
								personalBookList.dispose();
							}
						});
						
						personalBookList.add(b);
						personalBookList.add(delete);
					}
				}
					
				personalBookList.add(personalListHeader);
				personalBookList.add(close);
				personalBookList.setLayout(null);
				personalBookList.setSize(550, 600);
				personalBookList.setVisible(true);
				personalBookList.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			}
		};
	}
	
	private JLabel addHeader() {
		JLabel l = new JLabel("Book Broker");
		l.setBounds(10,10,150,20);
		l.setFont(new Font("Helvetica", Font.BOLD, 24));
		return l;
	}
	
	private JLabel addLoginHeader() {
		JLabel l = new JLabel("User Login");
		l.setBounds(20,20,250,40);
		l.setFont(new Font("Helvetica", Font.BOLD, 24));
		return l;
	}	
	private JTextField addSearchField() {
		JTextField tf = new JTextField("Search...");
		tf.setBounds(10,50,150,40);
		return tf;
	}
	private JTextField addUserField() {
		JTextField tf = new JTextField("Username");
		tf.setBounds(20,75,200,40);
		return tf;
	}
	private JTextField addPassField() {
		JTextField tf = new JTextField("Password");
		tf.setBounds(20,125,200,40);
		return tf;
	}
	private JComboBox<String> addSortBox() {
		String sortOptions[]  = {"Title A-Z", "Title Z-A", "Date Published"};
		JComboBox<String> cb = new JComboBox<String>(sortOptions);
		cb.setBounds(170, 50, 100, 39);
		return cb;
	}
	private JButton addLoginButton() {
		JButton b = new JButton("Login");
		b.setBounds(420, 10, 100, 40);
		return b;
	}
	private JButton addLogoutButton() {
		JButton b = new JButton("Logout");
		b.setBounds(420, 10, 100, 40);
		b.setVisible(false);
		return b;
	}
	private JButton addPersonalListButton() {
		JButton b = new JButton("Personal");
		b.setBounds(300, 10, 100, 40);
		return b;
	}
	private JButton addLoginEnterButton() {
		JButton b = new JButton("Enter");
		b.setBounds(20, 180, 80, 50);
		return b;
	}
	private JButton addLoginRegisterButton() {
		JButton b = new JButton("Register");
		b.setBounds(120, 180, 100, 50);
		return b;
	}
	
}