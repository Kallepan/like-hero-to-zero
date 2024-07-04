import pandas as pd


file = 'res/emissions.csv'


def read_csv(file_path: str) -> pd.DataFrame:
    return pd.read_csv(file_path)


def format_data(df: pd.DataFrame) -> pd.DataFrame:
    # transform year columns to rows
    df = df.melt(id_vars=['Country Name', 'Country Code', 'Indicator Name',
                 'Indicator Code'], var_name='Year', value_name='Value')

    # replace ' in country name
    df['Country Name'] = df['Country Name'].str.replace("'", "''")

    # set value to 0 if it is NaN
    df['Value'] = df['Value'].fillna(0)

    return df


def generate_sql(df: pd.DataFrame) -> str:
    # create table
    sql_string = """CREATE TABLE IF NOT EXISTS emission (
        id serial,
        country_code varchar,
        country_name varchar,
        year numeric,
        emission numeric
    );
    """
    # insert data
    insert = """INSERT INTO emission (country_code, country_name, year, emission)
    VALUES
    """

    for _, row in df.iterrows():
        insert += f"('{row['Country Code']}', '{row['Country Name']
                                                }', {row['Year']}, {row['Value']}),"

    insert = insert[:-1] + ';'
    sql_string += insert

    sql_string += """
    CREATE TABLE IF NOT EXISTS scientist (
        id serial,
        username varchar,
        password varchar
    );

    INSERT INTO scientist (username, password) VALUES ('admin', 'admin');
    """
    return sql_string


def to_file(sql: str) -> None:
    with open('res/emissions.sql', 'w') as file:
        file.write(sql)


def main():
    df = read_csv(file)
    df = format_data(df)
    sql = generate_sql(df)
    to_file(sql)


if __name__ == '__main__':
    main()
