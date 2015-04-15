[Setup]
AppName=Mensajero
AppVerName=Mensajero 0.1
DefaultDirName={pf}\Mensajero\Cliente
DefaultGroupName=Mensajero
UninstallDisplayIcon={app}\Mensajero.exe
Compression=lzma
SolidCompression=yes
OutputDir=userdocs:Inno Setup Examples Output

[Files]
Source: "cliente32.exe"; DestDir: "{app}";
Source: "cliente32.ini"; DestDir: "{app}";
Source: "cliente.jar"; DestDir: "{app}";
Source: "server32Start.bat"; DestDir: "{app}";
Source: "server32Stop.bat"; DestDir: "{app}";

;[Icons]
;Name: "{group}\mensajero"; Filename: "{app}\cliente16.exe"

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Registry]
Root: HKLM; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "Mensajero"; ValueData: """{app}\server32Start.bat"""; Flags: uninsdeletevalue

[Run]
Filename: "{app}\server32Start.bat"; Flags: nowait postinstall skipifsilent

[Code]
function InitializeSetup(): Boolean;
var
 ErrorCode: Integer;
 JavaInstalled : Boolean;
 ResultMsg : Boolean;
 Versions: TArrayOfString;
 I: Integer;
 regRoot: Integer;
begin
 // Check which view of registry should be taken:
 regRoot := HKLM
 begin
  if IsWin64 then
  begin
   regRoot := HKLM64
  end;
 end;
 if (RegGetSubkeyNames(regRoot, 'SOFTWARE\JavaSoft\Java Runtime Environment', Versions)) or (RegGetSubkeyNames(regRoot, 'SOFTWARE\JavaSoft\Java Development Kit', Versions)) then
 begin
  for I := 0 to GetArrayLength(Versions)-1 do
   if JavaInstalled = true then
   begin
    //do nothing
   end else
   begin
    if ( Versions[I][2]='.' ) and ( ( StrToInt(Versions[I][1]) > 1 ) or ( ( StrToInt(Versions[I][1]) = 1 ) and ( StrToInt(Versions[I][3]) >= 6 ) ) ) then
    begin
     JavaInstalled := true;
    end else
    begin
     JavaInstalled := false;
    end;
   end;
 end else
 begin
  JavaInstalled := false;
 end;

 if JavaInstalled then
 begin
  Result := true;
 end else
    begin
  ResultMsg := MsgBox('Oracle Java v1.6 or newer not found in the system. Java 1.7 or later is required to run this application (can be installed after this installation too). Do you want to continue?',
   mbConfirmation, MB_YESNO) = idYes;
  if ResultMsg = false then
  begin
   Result := false;
  end else
  begin
   Result := true;
   ShellExec('open',
    'http://www.java.com/getjava/',
    '','',SW_SHOWNORMAL,ewNoWait,ErrorCode);
  end;
    end;
end;

end.
