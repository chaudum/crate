.. highlight:: psql
.. _sql_lexical:

=================
Lexical Structure
=================

An SQL input consists of a sequence of commands each of which is a sequence of
tokens, terminated by a semicolon (``;``).

The syntax of a command defines its set of valid tokens. A token can be a key
word, an identifier, a quoted identifier, a literal (or constant), or a special
character symbol.

.. rubric:: Table of Contents

.. contents::
   :local:

.. _string_literal:

String Literal
==============

String literals are defined as an arbitrary sequence of characters that are
delimited with single quotes ``'`` as defined in ANSI SQL, for example
``'This is a string'``.

Escape Strings
--------------

The escape character in CrateDB is the single-quote ``'``. A character gets
escaped when adding a single-quote before it. For example a single quote
character within a string literal can be included by writing two adjacent
single quotes, e.g. ``'Jack''s car'``.

.. NOTE::

   Two adjacent single quotes are **not** equivalent to the double-quote
   character ``"``.

.. _sql_lexical_keywords_identifiers:

Key Words and Identifiers
=========================

The table bellow lists all **reserved key words** in CrateDB. These need to be
quoted if used as identifiers.

.. csv-table::
   :widths: 10, 10, 10, 10

    ADD, ALL, ALTER, AND
    ANY, ARRAY, AS, ASC
    BETWEEN, BOOLEAN, BY, BYTE
    CALLED, CASE, CAST, COLUMN
    CONSTRAINT, CREATE, CROSS, CURRENT_DATE
    CURRENT_SCHEMA, CURRENT_TIME, CURRENT_TIMESTAMP, CURRENT_USER
    DEFAULT, DELETE, DENY, DESC
    DESCRIBE, DIRECTORY, DISTINCT, DOUBLE
    DROP, ELSE, END, ESCAPE
    EXCEPT, EXISTS, EXTRACT, FALSE
    FIRST, FLOAT, FOR, FROM
    FULL, FUNCTION, GRANT, GROUP
    HAVING, IF, IN, INDEX
    INNER, INPUT, INSERT, INT
    INTEGER, INTERSECT, INTO, IP
    IS, JOIN, LAST, LEFT
    LIKE, LIMIT, LONG, MATCH
    NATURAL, NOT, NULL, NULLS
    OBJECT, OFFSET, ON, OR
    ORDER, OUTER, PERSISTENT, PRIMARY
    RECURSIVE, REPLACE, RESET, RETURNS
    REVOKE, RIGHT, SELECT, SESSION_USER
    SET, SHORT, SOME, STRATIFY
    STRING, SUBSTRING, TABLE, THEN
    TRANSIENT, TRUE, TRY_CAST, UNBOUNDED
    UNION, UPDATE, USER, USING
    WHEN, WHERE, WITH,

Tokens such as ``my_table``, ``id``, ``name``, or ``data`` in the example above
are **identifiers**, which identify names of tables, columns, and other
database objects.

Example::

    CREATE TABLE my_table (
      id INTEGER,
      name STRING,
      data OBJECT
    ) WITH (number_of_replicas = 0);

.. NOTE::

  Key words and unquoted identifiers are case insensitive.

This means that::

  select foo from t;

is equivalent to::

  select Foo from t;

or::

  select FOO from t;

A widely used convention is to write key words in uppercase and identifiers in
lowercase, such as

::

  ALTER TABLE foo ADD COLUMN new_column INTEGER;

::

  INSERT INTO foo (id, name) VALUES (1, 'bar');

Quoted identifiers can contain an arbitrary sequence of charactes enclosed by
double quotes (``"``). Quoted identifiers are never keywords, so you can use
``"update"`` as a table or column name.

.. _sql_lexical_special_chars:

Special Characters
==================

Some non-alphanumeric characters do have a special meaning. For their usage
please refer to the sections where the respective syntax elements are
described.

:Semicolon:
    The semicolon (``;``) terminates an SQL statement. It cannot appear
    anywhere else within the command, except within a string or quoted
    identifier.

:Comma:
    The comma (``,``) is used in various syntactical elements to separate
    elements of a list.

:Brackets:
    Square brackets (``[]``) are used to select elements of arrays and objects,
    e.g. ``arr[1]`` or ``obj['key']``.

:Asterisk:
    The asterisk (``*``) is used in some contexts to denote all columns of a
    table. As an argument in global aggregate functions it has the meaning of
    *any field*, e.g. ``COUNT(*)``.

:Period:
    The period (``.``) is used for numeric values and to separate schema and
    table names, e.g. ``blob.my_blob_table``.

.. _sql_lexical_comments:

Comments
========

An SQL statement can contain comments. Single line comments start with a double
dash (``--``) and end at the end of that line. Multi line comments start with
``/*`` and end with ``*/``.

Example::

  /*
   * Retrieve information about all tables in the 'doc' schema.
   */
  SELECT *
    FROM information_schema.tables
    WHERE table_schema = 'doc'; -- query information schema for doc tables

.. _`crash`: https://github.com/crate/crash/
