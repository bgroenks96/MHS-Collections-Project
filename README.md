##Madeira Historical Society Collections Project##

This software is part of a project to digitize and publicize historical artifacts for the educational benefit of the Madeira community and anyone else interested in the city's history.  All components of this project are written in The Java Programming Language, and any native code (although there currently is none) written in The C Programming Language.

Who is allowed to fork this project?
Anyone!  This project has produced some very interesting and useful code.  The shared-library in particular has a very resourceful codebase that could be applied to many other things.

Who is allowed to collaborate on this project?
Only selected individuals, probably involved with the Madeira Historical Society.  The information regarding server-handling and login is not public.

Project Source Components:

Editor -  The editor software for project participants to create, edit and upload artifact data to the database.  Uses the shared library.
Note: the editor requires FTP server login information, so it's mostly useful for only project collaborators.
Project status:  Stable

Editor-Launcher - The launcher for the editor software.
Project status:  Stable

Editor-Updater - The updater for the editor software.
Project status:  Stable

Shared-lib -  API containing classes shared/used by both the Editor and Applet.
Project status:  Stable

Applet - The front-end Java applet that will be loaded into the MHS webpage and utilized by end-users to view artifact information.  Uses the shared library.
Project status:  Stable (Deprecated)
