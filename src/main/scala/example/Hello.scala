package example

import java.nio.file.Paths
import java.sql.{Connection,DriverManager}
import java.sql.Timestamp
import java.time.Instant
import java.util.Date
import org.squeryl.{
  ForeignKeyDeclaration,
  KeyedEntity,
  KeyedEntityDef,
  Schema,
  Session,
  SessionFactory
}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLInnoDBAdapter
import org.squeryl.annotations.Column
import org.squeryl.dsl.{OneToMany,ManyToOne}
import os.{Path,RelPath}

object FileType extends Enumeration {
  type FileType = Value
  val File = Value(0, "File")
  val Directory = Value(1, "Directory")
}


class File(
  val path: String,
  val filename: String,
  val fileType: FileType.FileType,
  val parent: Long,
  val size: Long,
  val mtime: Timestamp,
  val atime: Timestamp,
  val ctime: Timestamp,
) extends KeyedEntity[Long] {
  def this() = this("", "",
    FileType.File, 0, 0,
    new Timestamp(0),
    new Timestamp(0),
    new Timestamp(0))
  var id = 0
  lazy val files: OneToMany[File] = FileDb.filesToFiles.left(this)
  lazy val parentDir: ManyToOne[File] = FileDb.filesToFiles.right(this)
}

object FileDb extends Schema {
  val files = table[File]
  val filesToFiles = oneToManyRelation(files, files)
    .via((o, m) => o.id === m.parent)
}

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
  var title: String,
  @Column("AUTHOR_ID")
  var authorId: Long,
  var coAuthorId: Option[Long]
) extends KeyedEntity[Long] {
  def this() = this("", 0, Some(0L))
  var id: Long = 0
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
        "jdbc:mysql://localhost:3306/dir2db?user=root&characterEncoding=UTF-8"),
      new MySQLInnoDBAdapter))

  transaction {
    Session.currentSession.setLogger(println)
    // println(Session.currentSession.databaseAdapter.supportsForeignKeyConstraints)
    // Library.create
    // Library.printDdl
    // authors.insert(new Author("Michel", "Folco", Some("folco@example.com")))
    // val a = from(authors)(a => where(a.lastName === "Folco") select a)
    // books.insert(new Book(0, "Can't Use Squeryl", 15, None))
    // println(a)
    // FileDb.printDdl
    // FileDb.create
    // val s = Session.currentSession.connection.createStatement
    // s.execute("""ALTER TABLE Book ADD
    //             |CONSTRAINT BookFK1
    //             |FOREIGN KEY (AUTHOR_ID) REFERENCES AUTHORS(id)""".stripMargin)
    // s.execute("""ALTER TABLE FILE ADD
    //             |CONSTRAINT FileFK1
    //             |FOREIGN KEY (parent) REFERENCES File(id)""".stripMargin)
    // s.execute("""ALTER TABLE File
    //             |CHARSET utf8mb4""".stripMargin)
    walk(".").foreach(println(_))
  }

  // def autoPath(str: String): Path = {
  //   val p = Paths.get(str)
  //   if (p.isAbsolute) Path(p) else RelPath(p)
  // }


  def walk(str: String) =
    for (path <- os.walk.stream(
      Path.expandUser(str, os.pwd),
      p => (Seq("project", "target") contains p.last) || p.last.startsWith("."),
      followLinks = true,
      includeTarget = true)
    ) yield {
      val info = os.stat.full(path)
      // os.perms
      // os.owner
      val mtime = Timestamp.from(info.mtime.toInstant)
      // println(s"${mtime}\t${info.atime}\t${info.ctime}")
      s"${mtime}\t${info.mtime}\t${path.last}"
    }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
