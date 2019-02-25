package example

import org.squeryl.{
  Session,
  SessionFactory,
  Schema,
  KeyedEntity,
  ForeignKeyDeclaration
}
import org.squeryl.dsl.{OneToMany,ManyToOne}
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import java.util.Date
import java.sql.Timestamp
import java.sql.{Connection,DriverManager}


class File (
  val id: Long,
  val path: String,
  val fielname: String,
  val fileType: String,
  val parent: Long,
  val size: Long,
  val mtime: Timestamp,
  val atime: Timestamp,
  val ctime: Timestamp,
)

class Author(
  val firstName: String,
  val lastName: String,
  val email: Option[String]
) extends KeyedEntity[Long] {
  def this() = this("","",Some(""))
  var id = 0
  lazy val books: OneToMany[Book] = Library.authorsToBooks.left(this)
}



class Book(
  var id: Long,
  var title: String,
  @Column("AUTHOR_ID")
  var authorId: Long,
  var coAuthorId: Option[Long]
) {
  def this() = this(0, "", 0, Some(0L))
  lazy val author: ManyToOne[Author] = Library.authorsToBooks.right(this)
}

object Library extends Schema {
  val authors = table[Author]("AUTHORS")
  val books = table[Book]
  val authorsToBooks =
    oneToManyRelation(authors, books)
      .via((a, b) => a.id === b.authorId)

  // override def applyDefaultForeignKeyPolicy(f: ForeignKeyDeclaration) =
  //   f.constrainReference
  // authorsToBooks.foreignKeyDeclaration.constrainReference(onDelete cascade)
}

import Library.{authors,books}

object Hello extends Greeting with App {
  Class.forName("com.mysql.jdbc.Driver")

  SessionFactory.concreteFactory = Some(() =>
    Session.create(
      java.sql.DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/dir2db?user=root"),
      new MySQLAdapter))

  transaction {
    Session.currentSession.setLogger(println)
    Library.create
    Library.printDdl
    authors.insert(new Author("Michel", "Folco", Some("folco@example.com")))
    val a = from(authors)(a => where(a.lastName === "Folco") select a)
    books.insert(new Book(0, "Can't Use Squeryl", 15, None))
    // println(a)
  }

  println(greeting)
  val wd = os.pwd
  for (path <- os.walk.stream(
    wd,
    p => (Seq("project", "target") contains p.last) || p.last.startsWith("."))
  ) {
    val info = os.stat.full(path)
    val mtime = Timestamp.from(info.mtime.toInstant)
    // println(s"${mtime}\t${info.atime}\t${info.ctime}")
    println(s"${mtime}\t${info.mtime}\t${path.last}")
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
