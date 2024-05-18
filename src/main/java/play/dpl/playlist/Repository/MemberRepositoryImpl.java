package play.dpl.playlist.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Member;

@Repository
public class MemberRepositoryImpl implements MemberRepository{
    
	@PersistenceContext
    private EntityManager entityManager;

	@Transactional
	@Override
	public Member save(Member member){
		entityManager.persist(member);

		return member;
	}

	@Transactional
	public Member updateMember(Member member){
		Query query = entityManager.createNativeQuery(
                "UPDATE MEMBER " +
                        "SET ACCESS_CODE = :accessCode, PLAYLIST_LIST = :playlistList "
                        +
                        "WHERE EMAIL = :email");
        query.setParameter("accessCode", member.getAccessCode());
        query.setParameter("playlistList", member.getPlaylistList());
        query.setParameter("email", member.getEmail());
        int flag = query.executeUpdate();
        if (flag != 1)
            throw new RuntimeException("BattleCustomRepoImpl 트랜잭션 롤백");

		return member;
	}
    @Override
    public Optional<Member> findById(String id) {
		System.out.println("MemberRepositoryImpl : findById("+id+")");
        Member member = entityManager.find(Member.class, id);
        return Optional.ofNullable(member);
    }

	@Override
	public void deleteAllByIdInBatch(Iterable<String> ids) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllByIdInBatch'");
	}

	@Override
	public void deleteAllInBatch() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
	}

	@Override
	public void deleteAllInBatch(Iterable<Member> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
	}

	@Override
	public <S extends Member> List<S> findAll(Example<S> example) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public <S extends Member> List<S> findAll(Example<S> example, Sort sort) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'flush'");
	}



	@Override
	public Member getOne(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getOne'");
	}

	@Override
	public Member getReferenceById(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getReferenceById'");
	}

	@Override
	public <S extends Member> List<S> saveAllAndFlush(Iterable<S> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'saveAllAndFlush'");
	}

	@Override
	public <S extends Member> S saveAndFlush(S entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'saveAndFlush'");
	}

	@Override
	public List<Member> findAll() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public List<Member> findAllById(Iterable<String> ids) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAllById'");
	}

	@Override
	public <S extends Member> List<S> saveAll(Iterable<S> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'count'");
	}

	@Override
	public void delete(Member entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'delete'");
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
	}

	@Override
	public void deleteAll(Iterable<? extends Member> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
	}

	@Override
	public void deleteAllById(Iterable<? extends String> ids) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllById'");
	}

	@Override
	public void deleteById(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
	}

	@Override
	public boolean existsById(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'existsById'");
	}

	@Override
	public List<Member> findAll(Sort sort) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public Page<Member> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public <S extends Member> long count(Example<S> example) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'count'");
	}

	@Override
	public <S extends Member> boolean exists(Example<S> example) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'exists'");
	}

	@Override
	public <S extends Member> Page<S> findAll(Example<S> example, Pageable pageable) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findAll'");
	}

	@Override
	public <S extends Member, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findBy'");
	}

	@Override
	public <S extends Member> Optional<S> findOne(Example<S> example) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findOne'");
	}
	@Override
	public Member getById(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getById'");
	}

   
}
